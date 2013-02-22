# Copyright (C) 2013 jOVAL.org.  All rights reserved.
# This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt
#
function Get-ServiceSids {
  foreach ($Service in Get-WmiObject Win32_Service) {
    sc.exe showsid $Service.Name
    Write-Output "ACCOUNT: $($Service.StartName)"
  }
}
