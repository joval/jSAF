# Copyright (C) 2012 jOVAL.org.  All rights reserved.
# This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt
#
function Find-Directories {
  param(
    [String]$Path = $PWD.Path,
    [String]$Pattern = ".*",
    [int]$Depth = 1 
  )

  if (Test-Path -literalPath $Path) {
    $CurrentItem = Get-Item -LiteralPath $Path -Force
    if ($CurrentItem.PSIsContainer) {
      if ($Path -imatch $Pattern) {
        $CurrentItem
      }
      if ($Depth -eq -1) {
        $ErrorActionPreference = "SilentlyContinue"
        Get-ChildItem $Path -Recurse -Force | Where-Object {$_.PSIsContainer -and ($_.FullName -imatch $Pattern)}
        $ErrorActionPreference = "Stop"
      } else {
        if ($Depth -ne 0) {
          $NextDepth = $Depth - 1
          Find-Directories -Path $CurrentItem.FullName -Pattern $Pattern -Depth $NextDepth
        }
      }
    }
  }
}

function Find-Files {
  [CmdletBinding(DefaultParameterSetName="Pattern")]param(
    [Parameter(ValueFromPipeline=$true)][PSObject]$CurrentItem,
    [String]$Path = $PWD.Path,
    [String]$Pattern = ".*",
    [int]$Depth = 1,
    [Parameter(ParameterSetName="Glob")][String]$FilenameGlob = $null,
    [Parameter(ParameterSetName="Pattern")][String]$Filename = ".*",
    [Parameter(ParameterSetName="Literal")][String]$LiteralFilename = ".*"
  )

  PROCESS {
    if ($CurrentItem -eq $null) {
      $CurrentItem = Get-Item -LiteralPath $Path -Force
    } else {
      $Path = $CurrentItem.FullName
    }
    if ($Depth -eq -1) {
      [System.GC]::Collect()
      $ErrorActionPreference = "SilentlyContinue"
      switch($PsCmdlet.ParameterSetName) {
        "Pattern" {
          if ($Pattern -eq ".*") {
            if ($Filename -eq ".*") {
              Get-ChildItem $Path -Recurse -Force
            } else {
              Get-ChildItem $Path -Recurse -Force | Where-Object {$_.Name -imatch $Filename}
            }
          } else {
            if ($Filename -eq ".*") {
              Get-ChildItem $Path -Recurse -Force | Where-Object {$_.FullName -imatch $Pattern}
            } else {
              Get-ChildItem $Path -Recurse -Force | Where-Object {$_.Name -imatch $Filename -and $_.FullName -imatch $Pattern}
            }
          }
        }
        "Glob" {
          Get-ChildItem $Path -Recurse -Force -Filter $FilenameGlob
        }
        default {
          Get-ChildItem $Path -Recurse -Force -Filter $LiteralFilename
        }
      }
      $ErrorActionPreference = "Stop"
    } else {
      if ($Pattern -eq ".*") {
        switch($PsCmdlet.ParameterSetName) {
          "Pattern" {
            if ($Filename -eq ".*") {
              $CurrentItem
            } elseif (!$CurrentItem.PSIsContainer -and ($CurrentItem.Name -imatch $Filename)) {
              $CurrentItem
            }
          }
          "Glob" {
            if ($FilenameGlob -eq "*") {
              $CurrentItem
            } else {
              $Glob = New-Object System.Management.Automation.WildcardPattern -ArgumentList $FilenameGlob
              if (!$CurrentItem.PSIsContainer -and ($Glob.IsMatch($CurrentItem.Name))) {
                $CurrentItem
              }
            }
          }
          default {
            if (!$CurrentItem.PSIsContainer -and ($CurrentItem.Name -eq $LiteralFilename)) {
              $CurrentItem
            }
          }
        }
      } else {
        if ($Path -imatch $Pattern) {
          $CurrentItem
        }
      }
      if ($CurrentItem.PSIsContainer -and ($Depth -ne 0)) {
        $NextDepth = $Depth - 1
        Get-ChildItem $CurrentItem -Force | %{
          if ($PsCmdlet.ParameterSetName -eq "Pattern") {
            Find-Files -Path $_.FullName -Pattern $Pattern -Filename $Filename -Depth $NextDepth
          } elseif ($PsCmdlet.ParameterSetName -eq "Glob") {
            Find-Files -Path $_.FullName -FilenameGlob $FilenameGlob -Depth $NextDepth
          } else {
            Find-Files -Path $_.FullName -LiteralFilename $LiteralFilename -Depth $NextDepth
          }
        }
      }
    }
  }
}

function Gzip-File {
  param (
    [String]$in = $(throw "Mandatory parameter -in missing."),
    [String]$out = $($in + ".gz")
  )
 
  if (Test-Path $in) {
    $input = New-Object System.IO.FileStream $in, ([IO.FileMode]::Open), ([IO.FileAccess]::Read), ([IO.FileShare]::Read)
    $output = New-Object System.IO.FileStream $out, ([IO.FileMode]::Create), ([IO.FileAccess]::Write), ([IO.FileShare]::None)
    $gzipStream = New-Object System.IO.Compression.GzipStream $output, ([IO.Compression.CompressionMode]::Compress)

    $buffer = New-Object byte[](512)
    $len = 0
    while(($len = $input.Read($buffer, 0, $buffer.Length)) -gt 0) {
      $gzipStream.Write($buffer, 0, $len)
    }
    $input.Close()
    $gzipStream.Close()
    $output.Close()
    Remove-Item $in
  }
}
