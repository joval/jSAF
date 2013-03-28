# Copyright (C) 2012 jOVAL.org.  All rights reserved.
# This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt
#
function Find-RegKeys {
  param(
    [String]$Hive = "HKEY_LOCAL_MACHINE",
    [String]$Key = "",
    [String]$Pattern = ".*",
    [String]$WithLiteralVal = "",
    [String]$WithEncodedVal = "",
    [String]$WithValPattern = "",
    [int]$Depth = 1 
  )

  if ($Key.Length -eq 0) {
    $FullPath = "Registry::$Hive"
  } else {
    $FullPath = "Registry::$Hive\$Key"
  }
  $ErrorActionPreference = "SilentlyContinue"
  $CurrentKey = Get-Item -literalPath $FullPath
  if ($CurrentKey -ne $null) {
    if ($Key -imatch $Pattern) {
      Filter-KeyConditions $CurrentKey -WithLiteralVal $WithLiteralVal -WithValPattern $WithValPattern -WithEncodedVal $WithEncodedVal
    }
    if ($Depth -eq -1) {
      Get-ChildItem $FullPath -recurse -force | Where-Object {$_.Name.Substring($_.Name.IndexOf("\") + 1) -imatch $Pattern} |
        Filter-KeyConditions -WithLiteralVal $WithLiteralVal -WithValPattern $WithValPattern -WithEncodedVal $WithEncodedVal
    } else {
      if ($Depth -gt 0) {
        $NextDepth = $Depth - 1
        foreach ($SubKeyName in $CurrentKey.GetSubKeyNames()) {
          if ($Key.Length -eq 0) {
            $SubKeyPath = $SubKeyName
          } else {
            $SubKeyPath = $Key + "\" + $SubKeyName
          }
          Find-RegKeys -Hive $Hive -Key $SubKeyPath -Pattern $Pattern -WithLiteralVal $WithLiteralVal -WithValPattern $WithValPattern -WithEncodedVal $WithEncodedVal -Depth $NextDepth
        }
      }
    }
  }
  $ErrorActionPreference = "Stop"
}

function Filter-KeyConditions {
  param(
    [Parameter(Mandatory=$true, Position=0, ValueFromPipeline=$true)][Microsoft.Win32.RegistryKey]$RegKey,
    [String]$WithLiteralVal = "",
    [String]$WithEncodedVal = "",
    [String]$WithValPattern = ""
  )

  PROCESS {
    if ($WithLiteralVal -ne "") {
      foreach ($ValName in $RegKey.GetValueNames()) {
        if ($ValName -eq $WithLiteralVal) {
          $RegKey
          break
        }
      }
    } else {
      if ($WithValPattern -ne "") {
        foreach ($ValName in $RegKey.GetValueNames()) {
          if ($ValName -imatch $WithValPattern) {
            $RegKey
            break
          }
        }
      } else {
        if ($WithEncodedVal -ne "") {
          $DecodedVal = [System.Convert]::FromBase64String($WithEncodedVal)
          foreach ($ValName in $RegKey.GetValueNames()) {
            if ($ValName -eq $DecodedVal) {
              $RegKey
              break
            }
          }
        } else {
          $RegKey
        }
      }
    }
  }
}
