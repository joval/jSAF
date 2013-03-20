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
  $Data = [System.Convert]::ToBase64String($Buffer.ToArray())
  Write-Output $Data
}
