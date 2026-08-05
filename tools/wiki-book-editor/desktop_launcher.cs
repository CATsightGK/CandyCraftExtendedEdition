using System;
using System.Diagnostics;
using System.IO;
using System.Reflection;
using System.Threading;

internal static class DesktopLauncher
{
    private static readonly string[] ResourceNames = { "node.exe", "server.js", "index.html", "style.css" };

    private static void Extract(string directory, string resourceName)
    {
        using (Stream source = Assembly.GetExecutingAssembly().GetManifestResourceStream(resourceName))
        using (FileStream target = File.Create(Path.Combine(directory, resourceName)))
        {
            source.CopyTo(target);
        }
    }

    private static string FindAppBrowser()
    {
        string[] candidates =
        {
            Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFilesX86), "Microsoft", "Edge", "Application", "msedge.exe"),
            Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles), "Microsoft", "Edge", "Application", "msedge.exe"),
            Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "Microsoft", "Edge", "Application", "msedge.exe"),
            Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles), "Google", "Chrome", "Application", "chrome.exe"),
            Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFilesX86), "Google", "Chrome", "Application", "chrome.exe")
        };
        foreach (string candidate in candidates)
        {
            if (File.Exists(candidate))
            {
                return candidate;
            }
        }
        return null;
    }

    private static void Main()
    {
        string directory = Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            "CandyCraftGuideBookEditor",
            DateTime.UtcNow.Ticks.ToString());
        Directory.CreateDirectory(directory);
        foreach (string resourceName in ResourceNames)
        {
            Extract(directory, resourceName);
        }
        int port = 43130 + Math.Abs(Environment.TickCount % 20000);
        ProcessStartInfo serverInfo = new ProcessStartInfo
        {
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
        string appBrowser = FindAppBrowser();
        if (appBrowser != null)
        {
            string profile = Path.Combine(directory, "profile");
            Directory.CreateDirectory(profile);
            Process.Start(new ProcessStartInfo
            {
                FileName = appBrowser,
                Arguments = "--app=\"" + url + "\" --user-data-dir=\"" + profile + "\" --no-first-run --disable-extensions",
                WorkingDirectory = directory,
                UseShellExecute = false,
                WindowStyle = ProcessWindowStyle.Normal
            });
        }
        else
        {
            Process.Start(url);
        }
    }
}
