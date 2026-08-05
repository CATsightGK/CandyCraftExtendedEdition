$ErrorActionPreference = "Stop"

$editorDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$desktop = [Environment]::GetFolderPath("Desktop")
$output = Join-Path $desktop "CandyCraft-Tool-Property-Editor.exe"
$stage = Join-Path $env:TEMP "CandyCraft-Tool-Property-Editor-stage"
$nodeCommand = Get-Command node -ErrorAction SilentlyContinue
if ($null -eq $nodeCommand) {
    $bundledNode = "C:\Users\10424\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\bin\node.exe"
    if (-not (Test-Path $bundledNode)) { throw "Node.js was not found" }
    $node = $bundledNode
} else {
    $node = $nodeCommand.Source
}
$csc = Join-Path $env:WINDIR "Microsoft.NET\Framework64\v4.0.30319\csc.exe"
if (-not (Test-Path $csc)) { throw "Microsoft C# compiler was not found" }

Remove-Item -LiteralPath $stage -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path $stage | Out-Null
Copy-Item -LiteralPath $node -Destination (Join-Path $stage "node.exe")
foreach ($name in @("server.js", "index.html", "style.css", "app.js")) {
    Copy-Item -LiteralPath (Join-Path $editorDir $name) -Destination $stage
}

try {
    $arguments = @("/nologo", "/target:winexe", "/optimize+", "/out:$output")
    foreach ($file in Get-ChildItem -LiteralPath $stage -File) {
        $arguments += "/resource:$($file.FullName),$($file.Name)"
    }
    $arguments += (Join-Path $editorDir "desktop_launcher.cs")
    & $csc @arguments
    if ($LASTEXITCODE -ne 0 -or -not (Test-Path $output)) { throw "EXE packaging failed with exit code $LASTEXITCODE" }
} finally {
    Remove-Item -LiteralPath $stage -Recurse -Force -ErrorAction SilentlyContinue
}

Write-Host "Created $output"
