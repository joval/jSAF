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
