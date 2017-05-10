# MT_POC_EPD_HV_INTEGRATION
##Master Thesis Proof of Concept - Electronic Patient Record (EPR) with HealthVault (Microsoft) Integration
03-04.2017


This little application (proof of concept) was developed as part of a master thesis' work for the BFH (Berner Fachhochschule - MAS Medizininformatik: https://www.ti.bfh.ch/index.php?id=6243).

The goal is to show how it is possible for a web application (EPR) to:
1. Connect to a measures store (Microsoft HealthVault) / Authentication
2. Allow a user to filter the data he wants
2. Extract measures
3. Convert them into a HL7 CDA
4. Display them to a user

##Authentication
This POC doesn't work out of the box because it doesn't contain the keystore (JKS) with a valid certificate. As this certificate is related to a specific developer, it is not enclosed here publicly.

