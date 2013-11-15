# Copyright (C) 2013 jOVAL.org.  All rights reserved.
# This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt
#
function Transfer-Encode {
  $Buffer = New-Object System.IO.MemoryStream
  $GZip = New-Object System.IO.Compression.GzipStream $Buffer, ([System.IO.Compression.CompressionMode]::Compress)
  $Out = New-Object System.IO.StreamWriter $GZip, (New-Object System.Text.UTF8Encoding)
  foreach($line in $input) {
    $Out.WriteLine($line)
  }
  $Out.Close()
  $GZip.Close()
  $Buffer.Close()
  Write-Output([System.Convert]::ToBase64String($Buffer.ToArray()))
}

function Load-Assembly {
  param(
    [String]$Data = $(throw "Mandatory parameter -Data missing.")
  )

  $MemStream = New-Object System.IO.MemoryStream (,[System.Convert]::FromBase64String($Data))
  $ZipStream = New-Object System.IO.Compression.GzipStream $MemStream, ([IO.Compression.CompressionMode]::Decompress)
  $Buffer = New-Object System.IO.MemoryStream

  $buff = New-Object byte[] 4096
  while (($len = $ZipStream.Read($buff, 0, 4096)) -gt 0) {
    $Buffer.Write($buff, 0, $len)
  }
  $Buffer.Close()
  [System.Reflection.Assembly]::Load($Buffer.ToArray())
}

function Check-Privileged {
  $Identity = [System.Security.Principal.WindowsIdentity]::GetCurrent()
  $Principal = New-Object System.Security.Principal.WindowsPrincipal($Identity)
  $Principal.IsInRole([System.Security.Principal.WindowsBuiltInRole]::Administrator)
}

function Safe-WmiQuery {
  param(
    [String]$Namespace = "root\cimv2",
    [String]$Query = $(throw "Mandatory parameter -Query missing."),
    [int]$Timeout = 0
  )

  if ($Timeout -eq 0) {
    Get-WmiObject -Namespace $Namespace -Query $Query
  } else {
    $ConnectionOptions = New-Object System.Management.ConnectionOptions
    $EnumerationOptions = New-Object System.Management.EnumerationOptions
    $EnumerationOptions.set_timeout([System.TimeSpan]::FromMilliseconds($Timeout))
    $Scope = New-Object System.Management.ManagementScope $Namespace, $ConnectionOptions
    $Scope.Connect()
    $Searcher = new-object System.Management.ManagementObjectSearcher
    $Searcher.set_options($EnumerationOptions)
    $Searcher.Query = new-object System.Management.ObjectQuery $Query
    $Searcher.Scope = $Scope
    trap { $_ } $Result = $Searcher.get()
    return $Result
  }
}
