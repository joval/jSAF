# Copyright (C) 2013 jOVAL.org.  All rights reserved.
# This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt
#
function Transfer-Encode {
  param (
    [Parameter(Mandatory=$true, Position=0, ValueFromPipeline=$true)]
    [String[]]$Input
  )
 
  BEGIN {
    $Buffer = New-Object System.IO.MemoryStream
    $GZip = New-Object System.IO.Compression.GzipStream $Buffer, ([System.IO.Compression.CompressionMode]::Compress)
    $Out = New-Object System.IO.StreamWriter $GZip
  }

  PROCESS {
    foreach($line in $Input) {
      $Out.WriteLine($line)
    }
  }

  END {
    $Out.Close()
    $GZip.Close()
    $Buffer.Close()
    $Data = [System.Convert]::ToBase64String($Buffer.ToArray())
    Write-Output $Data
  }
}
