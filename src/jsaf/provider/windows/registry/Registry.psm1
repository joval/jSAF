# Copyright (C) 2012 jOVAL.org.  All rights reserved.
# This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt
#
function Print-RegValues {
  param(
    [Parameter(Position=0, ValueFromPipeline=$true)]
    [string]$Key = "",
    [string]$Hive = "HKEY_LOCAL_MACHINE"
  )

  PROCESS {
    if ($Key.Length -eq 0) {
      $Path = $Hive
    } else {
      $Path = "$($Hive)\$($Key)"
    }
    $FullPath = "Registry::$($Path)"
    if (Test-Path -literalPath $FullPath) {
      Write-Output "[$($Path)]"
      $CurrentKey = Get-Item -literalPath $FullPath
      foreach ($Name in $CurrentKey.GetValueNames()) {
	Write-Output "{"
	Write-Output "Name: $($Name)"
	$Kind = $CurrentKey.GetValueKind($Name)
	Write-Output "Kind: $($Kind)"
	if ("Binary" -eq $Kind) {
	  Write-Output "Data: $([System.Convert]::ToBase64String($CurrentKey.GetValue($Name)))"
	} else {
	  if ("ExpandString" -eq $Kind) {
	    Write-Output "Data: $($CurrentKey.GetValue($Name, $null, 'DoNotExpandEnvironmentNames'))"
	  } else {
	    if ("MultiString" -eq $Kind) {
	      foreach ($Val in $CurrentKey.GetValue($Name)) {
		Write-Output "Data: $($Val)"
	      }
	    } else {
	      if ("DWord" -eq $Kind) {
		Write-Output "Data: $("{0:X8}" -f $CurrentKey.GetValue($Name))"
	      } else {
		if ("QWord" -eq $Kind) {
		   Write-Output "Data: $("{0:X16}" -f $CurrentKey.GetValue($Name))"
		} else {
		  Write-Output "Data: $($CurrentKey.GetValue($Name))"
		}
	      }
	    }
	  }
	}
	Write-Output "}"
      }
      Write-Output "[EOF]"
    }
  }
}
