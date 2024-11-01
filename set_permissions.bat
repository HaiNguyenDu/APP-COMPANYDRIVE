@echo off
icacls "\\192.168.10.25\Share\XPhuc\New folder (2)" /inheritance:r
icacls "\\192.168.10.25\Share\XPhuc\New folder (2)" /grant "Everyone:R"
icacls "\\192.168.10.25\Share\XPhuc\New folder (2)" /grant "PBL4\Administrators":F
