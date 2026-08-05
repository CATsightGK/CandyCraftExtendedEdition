param(
    [string]$ProjectRoot = (Split-Path $PSScriptRoot -Parent)
)

Add-Type -AssemblyName System.Drawing

function Open-Bitmap([string]$Path) {
    return [System.Drawing.Bitmap]::new((Resolve-Path $Path).Path)
}

function Save-Png([System.Drawing.Bitmap]$Bitmap, [string]$Path) {
    $directory = Split-Path $Path -Parent
    New-Item -ItemType Directory -Path $directory -Force | Out-Null
    $Bitmap.Save($Path, [System.Drawing.Imaging.ImageFormat]::Png)
}

function New-SignEntityTexture(
    [System.Drawing.Bitmap]$Mask,
    [System.Drawing.Bitmap]$Frame,
    [System.Drawing.Bitmap]$Center,
    [System.Drawing.Bitmap]$Stick,
    [string]$OutputPath
) {
    $scale = [Math]::Max(1, [int]($Frame.Width / 16))
    $output = [System.Drawing.Bitmap]::new(64 * $scale, 32 * $scale, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    try {
        for ($y = 0; $y -lt $output.Height; $y++) {
            for ($x = 0; $x -lt $output.Width; $x++) {
                $baseX = [int][Math]::Floor($x / $scale)
                $baseY = [int][Math]::Floor($y / $scale)
                if ($Mask.GetPixel($baseX, $baseY).A -eq 0) {
                    continue
                }

                $stickUv = $baseX -ge 0 -and $baseX -le 7 -and $baseY -ge 14 -and $baseY -le 29
                $faceStart = if ($baseX -ge 2 -and $baseX -le 25) { 2 } elseif ($baseX -ge 28 -and $baseX -le 51) { 28 } else { -1 }
                $insideFace = $faceStart -ge 0 -and $baseY -ge 2 -and $baseY -le 13
                $insideCenter = $insideFace -and ($baseX - $faceStart) -ge 2 -and ($baseX - $faceStart) -le 21 -and $baseY -ge 4 -and $baseY -le 11
                $source = if ($stickUv) { $Stick } elseif ($insideCenter) { $Center } else { $Frame }
                $color = $source.GetPixel($x % $source.Width, $y % $source.Height)
                $output.SetPixel($x, $y, $color)
            }
        }
        Save-Png $output $OutputPath
    } finally {
        $output.Dispose()
    }
}

function New-FramedSignItemTexture(
    [System.Drawing.Bitmap]$Frame,
    [System.Drawing.Bitmap]$Center,
    [string]$OutputPath
) {
    $scale = [Math]::Max(1, [int]($Frame.Width / 16))
    $output = [System.Drawing.Bitmap]::new(16 * $scale, 16 * $scale, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    try {
        for ($y = 0; $y -lt $output.Height; $y++) {
            for ($x = 0; $x -lt $output.Width; $x++) {
                $baseX = [int][Math]::Floor($x / $scale)
                $baseY = [int][Math]::Floor($y / $scale)
                $board = $baseX -ge 1 -and $baseX -le 14 -and $baseY -ge 2 -and $baseY -le 10
                $cutCorner = ($baseX -eq 1 -or $baseX -eq 14) -and ($baseY -eq 2 -or $baseY -eq 10)
                $stick = $baseX -ge 7 -and $baseX -le 8 -and $baseY -ge 11 -and $baseY -le 15
                if ((!$board -or $cutCorner) -and !$stick) {
                    continue
                }

                $insideCenter = $board -and $baseX -ge 3 -and $baseX -le 12 -and $baseY -ge 4 -and $baseY -le 8
                $source = if ($insideCenter) { $Center } else { $Frame }
                $color = $source.GetPixel($x % $source.Width, $y % $source.Height)
                $output.SetPixel($x, $y, $color)
            }
        }
        Save-Png $output $OutputPath
    } finally {
        $output.Dispose()
    }
}

function Get-AverageColor([System.Drawing.Bitmap]$Bitmap) {
    [long]$red = 0
    [long]$green = 0
    [long]$blue = 0
    [long]$count = 0
    for ($y = 0; $y -lt $Bitmap.Height; $y++) {
        for ($x = 0; $x -lt $Bitmap.Width; $x++) {
            $color = $Bitmap.GetPixel($x, $y)
            if ($color.A -gt 0) {
                $red += $color.R
                $green += $color.G
                $blue += $color.B
                $count++
            }
        }
    }
    return [System.Drawing.Color]::FromArgb(
        255,
        [int][Math]::Round($red / $count),
        [int][Math]::Round($green / $count),
        [int][Math]::Round($blue / $count)
    )
}

function Convert-HslToColor([double]$Hue, [double]$Saturation, [double]$Lightness, [int]$Alpha) {
    $chroma = (1.0 - [Math]::Abs(2.0 * $Lightness - 1.0)) * $Saturation
    $hueSection = $Hue / 60.0
    $secondary = $chroma * (1.0 - [Math]::Abs(($hueSection % 2.0) - 1.0))
    $red = 0.0
    $green = 0.0
    $blue = 0.0
    switch ([int][Math]::Floor($hueSection)) {
        0 { $red = $chroma; $green = $secondary }
        1 { $red = $secondary; $green = $chroma }
        2 { $green = $chroma; $blue = $secondary }
        3 { $green = $secondary; $blue = $chroma }
        4 { $red = $secondary; $blue = $chroma }
        default { $red = $chroma; $blue = $secondary }
    }
    $match = $Lightness - $chroma / 2.0
    return [System.Drawing.Color]::FromArgb(
        $Alpha,
        [int][Math]::Round(($red + $match) * 255.0),
        [int][Math]::Round(($green + $match) * 255.0),
        [int][Math]::Round(($blue + $match) * 255.0)
    )
}

function New-CherryBasedSignItemTexture(
    [System.Drawing.Bitmap]$Template,
    [System.Drawing.Bitmap]$Board,
    [System.Drawing.Bitmap]$Stick,
    [string]$OutputPath
) {
    $boardColor = Get-AverageColor $Board
    $output = [System.Drawing.Bitmap]::new($Template.Width, $Template.Height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    try {
        for ($y = 0; $y -lt $Template.Height; $y++) {
            for ($x = 0; $x -lt $Template.Width; $x++) {
                $templateColor = $Template.GetPixel($x, $y)
                if ($templateColor.A -eq 0) {
                    continue
                }
                if ($y -ge 11 -and $x -ge 7 -and $x -le 9) {
                    $bark = $Stick.GetPixel($x % $Stick.Width, $y % $Stick.Height)
                    $output.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($templateColor.A, $bark.R, $bark.G, $bark.B))
                } else {
                    $output.SetPixel($x, $y, (Convert-HslToColor $boardColor.GetHue() $boardColor.GetSaturation() $templateColor.GetBrightness() $templateColor.A))
                }
            }
        }
        Save-Png $output $OutputPath
    } finally {
        $output.Dispose()
    }
}

function Generate-Pack([string]$AssetsRoot, [System.Drawing.Bitmap]$Mask, [System.Drawing.Bitmap]$CherryItemTemplate) {
    $blockRoot = Join-Path $AssetsRoot 'textures\block'
    $entityRoot = Join-Path $AssetsRoot 'textures\entity\signs'
    $itemRoot = Join-Path $AssetsRoot 'textures\item'
    $definitions = @(
        @{ Name = 'marshmallow'; ItemName = 'marshmallow_sign'; Frame = 'marshmallow_planks.0.png'; Center = 'marshmallow_planks.0.png'; Stick = 'marshmallow_log_side.0.png'; CherryItem = $true },
        @{ Name = 'marshmallow_light'; ItemName = 'marshmallow_sign_light'; Frame = 'marshmallow_planks.2.png'; Center = 'marshmallow_planks.2.png'; Stick = 'marshmallow_log_side.2.png'; CherryItem = $true },
        @{ Name = 'marshmallow_dark'; ItemName = 'marshmallow_sign_dark'; Frame = 'marshmallow_planks.1.png'; Center = 'marshmallow_planks.1.png'; Stick = 'marshmallow_log_side.1.png'; CherryItem = $true },
        @{ Name = 'milk_chocolate'; ItemName = 'milk_chocolate_sign'; Frame = 'wafer_stick_side.png'; Center = 'chocolate_block_milk.png'; Stick = 'wafer_stick_side.png'; CherryItem = $false },
        @{ Name = 'white_chocolate'; ItemName = 'white_chocolate_sign'; Frame = 'wafer_stick_side.png'; Center = 'chocolate_block_white.png'; Stick = 'wafer_stick_side.png'; CherryItem = $false },
        @{ Name = 'dark_chocolate'; ItemName = 'dark_chocolate_sign'; Frame = 'wafer_stick_side.png'; Center = 'chocolate_block_dark.png'; Stick = 'wafer_stick_side.png'; CherryItem = $false }
    )

    foreach ($definition in $definitions) {
        $frame = Open-Bitmap (Join-Path $blockRoot $definition.Frame)
        $center = Open-Bitmap (Join-Path $blockRoot $definition.Center)
        $stick = Open-Bitmap (Join-Path $blockRoot $definition.Stick)
        try {
            New-SignEntityTexture $Mask $frame $center $stick (Join-Path $entityRoot ($definition.Name + '.png'))
            if ($definition.CherryItem) {
                New-CherryBasedSignItemTexture $CherryItemTemplate $center $stick (Join-Path $itemRoot ($definition.ItemName + '.png'))
            } else {
                New-FramedSignItemTexture $frame $center (Join-Path $itemRoot ($definition.ItemName + '.png'))
            }
        } finally {
            $frame.Dispose()
            $center.Dispose()
            $stick.Dispose()
        }
    }
}

function New-SignPreview([string]$AssetsRoot, [string[]]$Names, [string]$OutputPath) {
    $scale = 12
    $padding = 24
    $tileSize = 16 * $scale
    $width = $padding * 4 + $tileSize * 3
    $height = $padding * 2 + $tileSize
    $preview = [System.Drawing.Bitmap]::new($width, $height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    try {
        for ($y = 0; $y -lt $height; $y++) {
            for ($x = 0; $x -lt $width; $x++) {
                $checker = (([int]($x / 16) + [int]($y / 16)) % 2) -eq 0
                $preview.SetPixel($x, $y, $(if ($checker) { [System.Drawing.Color]::FromArgb(255, 37, 32, 39) } else { [System.Drawing.Color]::FromArgb(255, 48, 42, 50) }))
            }
        }

        for ($index = 0; $index -lt $Names.Count; $index++) {
            $icon = Open-Bitmap (Join-Path $AssetsRoot ('textures\item\' + $Names[$index]))
            try {
                $originX = $padding + $index * ($tileSize + $padding)
                for ($y = 0; $y -lt $tileSize; $y++) {
                    for ($x = 0; $x -lt $tileSize; $x++) {
                        $color = $icon.GetPixel([int][Math]::Floor($x / $scale), [int][Math]::Floor($y / $scale))
                        if ($color.A -gt 0) {
                            $preview.SetPixel($originX + $x, $padding + $y, $color)
                        }
                    }
                }
            } finally {
                $icon.Dispose()
            }
        }
        Save-Png $preview $OutputPath
    } finally {
        $preview.Dispose()
    }
}

$mask = Open-Bitmap (Join-Path $ProjectRoot 'build\tmp\oak_sign_reference.png')
$cherryItemTemplate = Open-Bitmap (Join-Path $ProjectRoot 'build\tmp\cherry_sign.png')
try {
    $defaultAssets = Join-Path $ProjectRoot 'src\main\resources\assets\candycraftmod'
    $classicAssets = Join-Path $ProjectRoot 'src\main\resources\resourcepacks\candycraft_classic\assets\candycraftmod'
    Generate-Pack $defaultAssets $mask $cherryItemTemplate
    Generate-Pack $classicAssets $mask $cherryItemTemplate
    New-SignPreview $defaultAssets @('milk_chocolate_sign.png', 'white_chocolate_sign.png', 'dark_chocolate_sign.png') (Join-Path $ProjectRoot 'build\previews\chocolate_signs.png')
    New-SignPreview $defaultAssets @('marshmallow_sign.png', 'marshmallow_sign_light.png', 'marshmallow_sign_dark.png') (Join-Path $ProjectRoot 'build\previews\marshmallow_signs.png')
} finally {
    $mask.Dispose()
    $cherryItemTemplate.Dispose()
}
