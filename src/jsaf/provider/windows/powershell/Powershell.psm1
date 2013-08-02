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

# Add-Type sporadically fails to work, so this is our implementation
function New-Type {
  param(
    [Parameter(Mandatory=$True,Position=1)][string]$TypeDefinition=$(throw "Mandatory parameter -TypeDefinition missing.")
  )

  $Params = New-Object System.CodeDom.Compiler.CompilerParameters
  $Params.ReferencedAssemblies.AddRange($(@("System.dll", $([PSObject].Assembly.Location))))
  $Params.GenerateInMemory = $True
  $Temp = $(Get-Item ENV:TEMP).Value
  $Params.TempFiles = New-Object System.CodeDom.Compiler.TempFileCollection $Temp, $False

  $Provider = New-Object Microsoft.CSharp.CSharpCodeProvider
  Try {
    $CompilerResults = $Provider.CompileAssemblyFromSource($Params, $TypeDefinition)
    if ($CompilerResults.Errors.Count -gt 0) {
      $CodeLines = $TypeDefinition -Split '[\n]';
      $ErrorMessage = "Compilation Errors:"
      foreach ($CompileError in $CompilerResults.Errors) {
        $ErrorMessage = [String]::Concat($ErrorMessage, "`n", $CompileError.ToString())
      }
      Write-Error $ErrorMessage
    }
  } Catch [System.Exception] {
    if ($_.GetType().ToString() -eq "System.Management.Automation.ErrorRecord") {
      Write-Error $_.Exception.ToString()
    } else {
      Write-Error $_.ToString()
    }
  }
}
