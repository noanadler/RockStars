function Get-Countries
{
    $Response = Invoke-WebRequest http://wwwnc.cdc.gov/travel/destinations/list

    $LinksPages = $Response.ParsedHTML.body.getElementsByClassName("row links-pages")

    $Countries = @()
    for($i = 0; $i -lt $LinksPages.Length; ++$i)
    {
        $row = ($LinksPages.item($i) | select "textContent").textContent.split("`n")
        $innerHTML = ($LinksPages.item($i) | select "innerHTML").innerHTML.split("`n")
        for($j = 4; ($row[$j]).Trim().length -gt 0; ++$j){
            if(!($row[$j]).contains("(see"))
            {
                $Country = ($row[$j]).Trim()
                $URL = (($InnerHTML | ? { $_.Contains($Country)}).split("`""))[1] 

                $details = @{            
                Country   = $Country            
                URL  = $URL
                }
                $Countries += New-Object PSObject -Property $details
            }
        }
    }
    return $Countries
}

function Get-RoutineVaccines
{
    param($Type)
    if($Type -eq "Child"){
        $Selector = 2
    }else{
        $Selector = 4
    }
    $Response = Invoke-WebRequest http://wwwnc.cdc.gov/travel/diseases/routine
    $LinksPages = $Response.ParsedHTML.body.getElementsByClassName("span19") | select "innerHTML"
    $InnerHTML = $LinksPages.item(3).innerHTML
    $VaccinesHTML = ($InnerHTML -split "<ul>|</ul>")[$Selector] -split "<li>|</li>"

    $Vaccines = @()
    for($i = 0; $i -lt $VaccinesHTML.Length; ++$i)
    {
        if($VaccinesHTML[$i].Trim().Length -gt 0)
        {
            $Vaccine = $VaccinesHTML[$i] -replace '</a>',"" -replace '.+>',"" 
            $Link = $VaccinesHTML[$i] -replace '<a href="',""  -replace '">.*',""
            
            $details = @{            
                Vaccine   = $Vaccine             
                Link  = $Link
            } 
            
            $Vaccines += New-Object PSObject -Property $details 
        }
    }
    return $Vaccines
}

function Get-VaccineRecommendationsByCountry
{
    param ($Response)

    $LinksPages = $Response.ParsedHTML.body.getElementsByTagName('td') |  Where {$_.getAttributeNode('class').Value -match 'group-head|traveler-disease|traveler-findoutwhy'}
    $CountryDiseases = @()
    for($i = 0; $i -lt $LinksPages.Length; ++$i)
    {
        if($LinksPages[$i].className -eq 'group-head')
        {
            $CurrentGroup = $LinksPages[$i].innerText.split("`n")
            $CurrentGroupName = $CurrentGroup[0].Trim()
            $CurrentGroupDescriptor = ($LinksPages[$i].innerText -replace $CurrentGroupName,"").trim()         
            
        }elseif($LinksPages[$i].className -eq 'traveler-disease')
        {
            $DiseaseName = $LinksPages[$i].innerText
        }else
        {
            $Why = $LinksPages[$i].innerText

            $details = @{            
                Travelers   = $CurrentGroupName             
                DiseaseName  = $DiseaseName                
                Details      = $Why
            }                           
            $CountryDiseases += New-Object PSObject -Property $details 
        }
    }

    return ($CountryDiseases | ConvertTo-Json)
}

function Get-PackingListByCountry
{
    param ($Response)

    $LinksPages = $Response.ParsedHTML.body.getElementsByTagName('ul') |  Where {$_.className -match 'packing'}
    $Categories = $Response.ParsedHTML.body.getElementsByTagName('h4') |  Where {$_.className -match 'traveler-text-color'}
    $PackingList = @()

    for($i = 0; $i -lt $LinksPages.Length; ++$i)
    {
       $Category = $Categories[$i].innerText
       $Items = ($LinksPages[$i]).innerHTML -split "<li>|</li>" 
       foreach($Item in $Items)
       {
            if($Item.Length -gt 29)
            {
                if($Item.contains("<span>"))
                {
                    $ItemNotes = (($Item -split "</span><br>")[1] -replace "<span>","" -replace "</span>","" -replace '<p">',"" -replace '</p">',"").trim()
                }
                $ItemName = (($Item -split "</span><br>")[0] -replace '<span style="font-weight: bold;">',"" -replace "</a>","" -replace '<a href="[^>]+>',"").trim()
                $details = @{            
                    Item   = $ItemName             
                    Notes  = $ItemNotes                
                    Category      = $Category
                } 
                $PackingList += New-Object PSObject -Property $details
                $ItemNotes = ""
            }

       }
    }

    return ($PackingList | ConvertTo-Json)
}

#Get list of countries/associated CDC page links
$Countries = Get-Countries

#Get routine vaccine lists for adults and children and output to CSV
Get-RoutineVaccines -Type "Child" | export-csv -Path ("RoutineChildVaccines.csv") -NoTypeInformation

Get-RoutineVaccines -Type "Adult" | export-csv -Path ("RoutineAdultVaccines.csv") -NoTypeInformation



#Get other info for each country
$CountryTable = @()
$Countries | % { 
    echo ($_.Country + "`n")
    
    #Get diseases/vaccines
    $Link = $_.URL
    $Response = Invoke-WebRequest ("http://wwwnc.cdc.gov$Link")
    $CountryVaccines = Get-VaccineRecommendationsByCountry -Response $Response
        
    #Get packing list
    $Link = (($Response.ParsedHTML.body.getElementsByClassName("section_body") | select "innerHTML").innerHTML -join "" -split '<p>Use the <a href="|">Healthy')[1]
    $Response = Invoke-WebRequest ("http://wwwnc.cdc.gov$Link")
    $CountryPackingList = Get-PackingListByCountry -Response $Response

    $details = @{            
                    Country   = $_.Country            
                    RecommendedVaccines  = $CountryVaccines               
                    PackingList      = $CountryPackingList
                } 
    
    $CountryTable += New-Object PSObject -Property $details

    $details
}

$CountryTable | export-csv -Path ("Countries.csv") -NoTypeInformation

