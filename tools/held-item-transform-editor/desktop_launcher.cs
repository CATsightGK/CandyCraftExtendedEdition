using System;
using System.Diagnostics;
using System.IO;
using System.Reflection;
using System.Threading;

internal static class DesktopLauncher
{
    private static void ExtractAll(string directory)
    {
        Assembly assembly = Assembly.GetExecutingAssembly();
        foreach (string name in assembly.GetManifestResourceNames())
        {
            string targetPath = Path.Combine(directory, name.Replace('/', Path.DirectorySeparatorChar));
            string parent = Path.GetDirectoryName(targetPath);
            if (!String.IsNullOrEmpty(parent)) Directory.CreateDirectory(parent);
            if (name.Equals("node.exe", StringComparison.OrdinalIgnoreCase) && File.Exists(targetPath)) continue;
            using (Stream source = assembly.GetManifestResourceStream(name))
            using (FileStream target = File.Create(targetPath)) source.CopyTo(target);
        }
    }

    private static string FindAppBrowser()
    {
        string[] candidates = {
            Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFilesX86), "Microsoft", "Edge", "Application", "msedge.exe"),
            Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles), "Microsoft", "Edge", "Application", "msedge.exe"),
            Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "Microsoft", "Edge", "Application", "msedge.exe"),
            Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles), "Google", "Chrome", "Application", "chrome.exe"),
            Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFilesX86), "Google", "Chrome", "Application", "chrome.exe")
        };
        foreach (string candidate in candidates) if (File.Exists(candidate)) return candidate;
        return null;
    }

    private static void Main()
    {
        string baseDirectory = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "CandyCraftHeldItemEditor");
        string directory = Path.Combine(baseDirectory, "app");
        Directory.CreateDirectory(directory);
        ExtractAll(directory);
        int port = 4315;
        ProcessStartInfo serverInfo = new ProcessStartInfo {
            FileName = Path.Combine(directory, "node.exe"),
            Arguments = "server.js --no-browser",
            WorkingDirectory = directory,
            CreateNoWindow = true,
            UseShellExecute = false,
            WindowStyle = ProcessWindowStyle.Hidden
        };
        serverInfo.EnvironmentVariables["CANDYCRAFT_NO_BROWSER"] = "1";
        serverInfo.EnvironmentVariables["PORT"] = port.ToString();
        Process.Start(serverInfo);
        Thread.Sleep(900);

        string url = "http://127.0.0.1:" + port + "/?app=1";
        string browser = FindAppBrowser();
        if (browser == null) { Process.Start(url); return; }
        string profile = Path.Combine(baseDirectory, "profile");
        Directory.CreateDirectory(profile);
        Process.Start(new ProcessStartInfo {
            FileName = browser,
            Arguments = "--app=\"" + url + "\" --user-data-dir=\"" + profile + "\" --no-first-run --disable-extensions",
            WorkingDirectory = directory,
            UseShellExecute = false,
            WindowStyle = ProcessWindowStyle.Normal
        });
    }
}
