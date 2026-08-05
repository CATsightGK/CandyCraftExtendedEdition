using System;
using System.Diagnostics;
using System.IO;
using System.Net.Sockets;
using System.Reflection;

internal static class DesktopLauncher
{
    private static readonly string[] Resources = { "node.exe", "server.js", "index.html", "style.css" };

    private static bool IsEditorRunning()
    {
        try
        {
            using (TcpClient client = new TcpClient())
            {
                IAsyncResult result = client.BeginConnect("127.0.0.1", 4326, null, null);
                bool connected = result.AsyncWaitHandle.WaitOne(250);
                if (connected) client.EndConnect(result);
                return connected;
            }
        }
        catch { return false; }
    }

    private static void OpenEditor()
    {
        Process.Start(new ProcessStartInfo {
            FileName = "rundll32.exe",
            Arguments = "url.dll,FileProtocolHandler http://127.0.0.1:4326/",
            CreateNoWindow = true,
            UseShellExecute = false,
            WindowStyle = ProcessWindowStyle.Hidden
        });
    }

    private static void Main()
    {
        if (IsEditorRunning())
        {
            if (Environment.GetEnvironmentVariable("CANDYCRAFT_NO_BROWSER") != "1") OpenEditor();
            return;
        }

        string directory = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            "CandyCraftRecipeEditor", "runtime");
        Directory.CreateDirectory(directory);
        foreach (string name in Resources)
        {
            using (Stream source = Assembly.GetExecutingAssembly().GetManifestResourceStream(name))
            using (FileStream target = File.Create(Path.Combine(directory, name))) source.CopyTo(target);
        }
        Process.Start(new ProcessStartInfo {
            FileName = Path.Combine(directory, "node.exe"),
            Arguments = Environment.GetEnvironmentVariable("CANDYCRAFT_NO_BROWSER") == "1" ? "server.js --no-browser" : "server.js",
            WorkingDirectory = directory,
            CreateNoWindow = true,
            UseShellExecute = false,
            WindowStyle = ProcessWindowStyle.Hidden
        });
    }
}
