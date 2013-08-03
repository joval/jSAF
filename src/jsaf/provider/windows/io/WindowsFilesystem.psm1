# Copyright (C) 2012 jOVAL.org.  All rights reserved.
# This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt
#
function Print-FileInfo {
  param(
    [Parameter(Mandatory=$true, Position=0, ValueFromPipeline=$true)][PSObject]$inputObject
  )

  PROCESS {
    if (!($inputObject -eq $null)) {
      $type = $inputObject | Get-Member | %{$_.TypeName}
      if ($type -eq "System.String") {
	if (Test-Path $inputObject) {
	  Get-Item $inputObject | Print-FileInfo
	} else {
          "{"
          "Path: {0}" -f $inputObject
          "}"
	}
      } else {
	$OwnerSid = $inputObject.GetAccessControl().GetOwner([System.Security.Principal.SecurityIdentifier]).ToString()
	$OwnerAccount = $inputObject.GetAccessControl().GetOwner([System.Security.Principal.NTAccount]).ToString()
        if ($type -eq "System.IO.DirectoryInfo") {
          "{"
          "Type: Directory"
          $path = [jSAF.File.Probe]::GetWindowsPhysicalPath($inputObject.FullName)
          "Path: $path"
	  "Owner.SID: " -f $OwnerSid
	  "Owner.Account: $($OwnerAccount)"
          "Ctime: {0:D}" -f $inputObject.CreationTimeUtc.toFileTimeUtc()
          "Mtime: {0:D}" -f $inputObject.LastWriteTimeUtc.toFileTimeUtc()
          "Atime: {0:D}" -f $inputObject.LastAccessTimeUtc.toFileTimeUtc()
          "}"
        } else {
          if ($type -eq "System.IO.FileInfo") {
            "{"
            "Type: File"
            $path = [jSAF.File.Probe]::GetWindowsPhysicalPath($inputObject.FullName)
            "WinType: {0:D}" -f [jSAF.File.Probe]::GetFileType($path)
            "Path: {0}" -f $path
	    "Owner.SID: {0}" -f $OwnerSid
	    "Owner.Account: {0}" -f $OwnerAccount
            "Ctime: {0:D}" -f $inputObject.CreationTimeUtc.toFileTimeUtc()
            "Mtime: {0:D}" -f $inputObject.LastWriteTimeUtc.toFileTimeUtc()
            "Atime: {0:D}" -f $inputObject.LastAccessTimeUtc.toFileTimeUtc()
            $length = $inputObject.Length
            "Length: {0:D}" -f $length
            if ($length -gt 0) {
              "pe.MSChecksum: {0:D}" -f [jSAF.File.Probe]::GetChecksum($Path)
            }
            $Info = [System.Diagnostics.FileVersionInfo]::GetVersionInfo($path)
            if ($Info.FileVersion -ne $null) {
              "pe.FileVersion: {0}" -f $Info.FileVersion
              "pe.FileMajorPart: {0:D}" -f $Info.FileMajorPart
              "pe.FileMinorPart: {0:D}" -f $Info.FileMinorPart
              "pe.FileBuildPart: {0:D}" -f $Info.FileBuildPart
              "pe.FilePrivatePart: {0:D}" -f $Info.FilePrivatePart
            }
            if ($Info.ProductName -ne $null) {
              "pe.ProductName: {0}" -f $Info.ProductName
            }
            if ($Info.ProductVersion -ne $null) {
              "pe.ProductVersion: {0}" -f $Info.ProductVersion
            }
            if ($Info.CompanyName -ne $null) {
              "pe.CompanyName: {0}" -f $Info.CompanyName
            }
            if ($Info.Language -ne $null) {
              "pe.Language: {0}" -f $Info.Language
            }
            if ($Info.OriginalFilename -ne $null) {
              "pe.OriginalFilename: {0}" -f $Info.OriginalFilename
            }
            if ($Info.InternalName -ne $null) {
              "pe.InternalName: {0}" -f $Info.InternalName
            }
            "}"
          }
        }
      }
    }
  }
}
