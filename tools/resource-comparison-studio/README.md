# CandyCraft Resource Comparison Studio

Run `start_studio.bat`, or run:

```powershell
node server.js
```

The studio reads both resource roots directly from the CandyCraft project:

- Current: `src/main/resources/assets/candycraftmod`
- Classic: `src/main/resources/resourcepacks/candycraft_classic/assets/candycraftmod`

Use `build_desktop_exe.ps1` to create the desktop launcher. The launcher keeps
the project path used during packaging, so the resource files remain live and
can be refreshed without rebuilding the executable.
