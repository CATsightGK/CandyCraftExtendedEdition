using System;
using System.Diagnostics;
using System.IO;
using System.Reflection;

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

    private static void Main()
    {
        string directory = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            "CandyCraftAdvancementEditor", DateTime.UtcNow.Ticks.ToString());
        Directory.CreateDirectory(directory);
        foreach (string resourceName in ResourceNames) Extract(directory, resourceName);
        Process.Start(new ProcessStartInfo
        {
            FileName = Path.Combine(directory, "node.exe"),
            Arguments = Environment.GetEnvironmentVariable("CANDYCRAFT_NO_BROWSER") == "1" ? "server.js --no-browser" : "server.js",
            WorkingDirectory = directory,
            CreateNoWindow = true,
            UseShellExecute = false,
            WindowStyle = ProcessWindowStyle.Hidden
        });
    }
}
