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

# Add-Type sporadically fails to work, so this is our implementation
function New-Type {
  param(
    [string]$TypeDefinition=$(throw "Mandatory parameter -TypeDefinition missing."),
    [string[]]$ReferencedAssemblies
  )

  $provider = New-Object Microsoft.CSharp.CSharpCodeProvider
  $dllName = [PsObject].Assembly.Location
  $compilerParameters = New-Object System.CodeDom.Compiler.CompilerParameters
  $assemblies = @("System.dll", $dllName)
  $compilerParameters.ReferencedAssemblies.AddRange($assemblies)
  if($ReferencedAssemblies) {
     $compilerParameters.ReferencedAssemblies.AddRange($ReferencedAssemblies)
  }
  $compilerParameters.IncludeDebugInformation = $true
  $compilerParameters.GenerateInMemory = $true
  $compilerResults = $provider.CompileAssemblyFromSource($compilerParameters, $TypeDefinition)
  if($compilerResults.Errors.Count -gt 0) {
    $compilerResults.Errors | % { Write-Error ("{0}:`t{1}" -f $_.Line,$_.ErrorText) }
  }
}
