using System;
using System.Diagnostics;
using System.IO;
using System.Net;
using System.Reflection;
using System.Threading;

internal static class DesktopLauncher
{
    private static string LogPath(string baseDirectory)
    {
        return Path.Combine(baseDirectory, "launcher.log");
    }

    private static void Log(string baseDirectory, string message)
    {
        try
        {
            File.AppendAllText(LogPath(baseDirectory), DateTime.Now.ToString("O") + " " + message + Environment.NewLine);
        }
        catch { }
    }

    private static void ExtractAll(string directory)
    {
        Assembly assembly = Assembly.GetExecutingAssembly();
        foreach (string name in assembly.GetManifestResourceNames())
        {
            string targetPath = Path.Combine(directory, name.Replace('/', Path.DirectorySeparatorChar));
            string parent = Path.GetDirectoryName(targetPath);
            if (!String.IsNullOrEmpty(parent)) Directory.CreateDirectory(parent);
            if (String.Equals(name, "node.exe", StringComparison.OrdinalIgnoreCase) && File.Exists(targetPath))
                continue;
            using (Stream source = assembly.GetManifestResourceStream(name))
            using (FileStream target = File.Create(targetPath)) source.CopyTo(target);
        }
    }

    private static string FindBrowser()
    {
        string[] candidates = {
            Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFilesX86), "Microsoft", "Edge", "Application", "msedge.exe"),
            Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles), "Microsoft", "Edge", "Application", "msedge.exe"),
            Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "Microsoft", "Edge", "Application", "msedge.exe"),
            @"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe",
            @"C:\Program Files\Microsoft\Edge\Application\msedge.exe",
            Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles), "Google", "Chrome", "Application", "chrome.exe"),
            Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFilesX86), "Google", "Chrome", "Application", "chrome.exe"),
            @"C:\Program Files\Google\Chrome\Application\chrome.exe",
            @"C:\Program Files (x86)\Google\Chrome\Application\chrome.exe"
        };
        foreach (string candidate in candidates) if (File.Exists(candidate)) return candidate;
        return null;
    }

    private static bool ServerReady(string url)
    {
        try
        {
            HttpWebRequest request = (HttpWebRequest)WebRequest.Create(url + "api/index");
            request.Timeout = 600;
            using (HttpWebResponse response = (HttpWebResponse)request.GetResponse())
                return response.StatusCode == HttpStatusCode.OK;
        }
        catch { return false; }
    }

    private static void Main()
    {
        string baseDirectory = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "CandyCraftResourceStudio");
        string appDirectory = Path.Combine(baseDirectory, "app");
        Directory.CreateDirectory(appDirectory);
        Log(baseDirectory, "launcher start");
        try
        {
            ExtractAll(appDirectory);
            Log(baseDirectory, "resources extracted");
        }
        catch (Exception error)
        {
            Log(baseDirectory, "resource extraction failed: " + error);
        }
        const int port = 4321;
        string url = "http://127.0.0.1:" + port + "/";
        if (!ServerReady(url))
        {
            ProcessStartInfo serverInfo = new ProcessStartInfo {
                FileName = Path.Combine(appDirectory, "node.exe"),
                Arguments = "server.js --no-browser",
                WorkingDirectory = appDirectory,
                CreateNoWindow = true,
                UseShellExecute = false,
                WindowStyle = ProcessWindowStyle.Hidden
            };
            serverInfo.EnvironmentVariables["CANDYCRAFT_NO_BROWSER"] = "1";
            serverInfo.EnvironmentVariables["PORT"] = port.ToString();
            Process.Start(serverInfo);
            Log(baseDirectory, "server process started");
            for (int attempt = 0; attempt < 12 && !ServerReady(url); attempt++) Thread.Sleep(250);
        }
        Log(baseDirectory, "server ready=" + ServerReady(url));
        string browser = FindBrowser();
        if (browser == null)
        {
            Log(baseDirectory, "no supported browser found; opening default URL");
            Process.Start(new ProcessStartInfo { FileName = url, UseShellExecute = true });
            return;
        }
        Log(baseDirectory, "browser=" + browser);
        string profile = Path.Combine(baseDirectory, "profile");
        Directory.CreateDirectory(profile);
        try
        {
            Process.Start(new ProcessStartInfo {
                FileName = browser,
                Arguments = "--app=\"" + url + "\" --user-data-dir=\"" + profile + "\" --no-first-run --disable-extensions --new-window",
                WorkingDirectory = appDirectory,
                UseShellExecute = true,
                WindowStyle = ProcessWindowStyle.Normal
            });
            Log(baseDirectory, "browser launch requested");
        }
        catch (Exception error)
        {
            Log(baseDirectory, "browser launch failed: " + error);
            Process.Start(new ProcessStartInfo { FileName = url, UseShellExecute = true });
        }
    }
}
