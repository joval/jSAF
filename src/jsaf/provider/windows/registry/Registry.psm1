# Copyright (C) 2012 jOVAL.org.  All rights reserved.
# This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt
#
function Print-RegValues {
  param(
    [Parameter(Position=0, ValueFromPipeline=$true)][string]$Key = "",
    [string]$Hive = "HKEY_LOCAL_MACHINE"
  )

  PROCESS {
    if ($Key.Length -eq 0) {
      $Path = $Hive
    } else {
      $Path = "{0}\{1}" -f $Hive, $Key
    }
    $FullPath = "Registry::$($Path)"
    if (Test-Path -literalPath $FullPath) {
      "[{0}]" -f $Path
      try {
	$ErrorActionPreference = "Stop"
        $CurrentKey = Get-Item -literalPath $FullPath
        foreach ($Name in $CurrentKey.GetValueNames()) {
          "{"
          "Name: {0}" -f $Name
          $Kind = $CurrentKey.GetValueKind($Name)
          "Kind: {0}" -f $Kind.ToString()
	  switch($Kind) {
	    Binary {"Data: {0}" -f [System.Convert]::ToBase64String($CurrentKey.GetValue($Name))}
            ExpandString {"Data: {0}" -f $CurrentKey.GetValue($Name, $null, "DoNotExpandEnvironmentNames")}
            MultiString {
              foreach ($Val in $CurrentKey.GetValue($Name)) {
                "Data: {0}" -f $Val
              }
	    }
            DWord {"Data: {0:X8}" -f $CurrentKey.GetValue($Name)}
            QWord {"Data: {0:X16}" -f $CurrentKey.GetValue($Name)}
	    Default {"Data: $($CurrentKey.GetValue($Name))"}
          }
          "}"
        }
      } catch {
	"Error: {0}" -f $Error[0].Exception.Message
      } finally {
	$ErrorActionPreference = "Continue";
        "[EOF]"
      }
    }
  }
}
