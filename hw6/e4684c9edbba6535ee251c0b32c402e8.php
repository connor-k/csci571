<!DOCTYPE html>
<html lang="en">
<html>
<head>
<meta charset="UTF-8">
<title>ckerns HW4 - XML</title>
</head>
<body>
<?php
    // Lookup
    $lookup_baseurl = 'http://dev.markitondemand.com/MODApis/Api/v2/Lookup/xml?input=';
    $input = 'GOOG';
    // TODO would http_get be better?
    $lookup_response = file_get_contents($lookup_baseurl.$input);
    $xml_root = new SimpleXMLElement($lookup_response);
    // TODO Check not empty
    echo "Got XML response with ",sizeof($xml_root->LookupResult)," entries:<br>";
    for ($i = 0; $i < sizeof($xml_root->LookupResult); $i++) {
        echo "Entry $i:<br>";
        echo $xml_root->LookupResult[$i]->Name,"<br>";
        echo $xml_root->LookupResult[$i]->Symbol,"<br>";
        echo $xml_root->LookupResult[$i]->Exchange,"<br><br>";
    }
    
    // Quote
    $quote_baseurl = 'http://dev.markitondemand.com/MODApis/Api/v2/Quote/json?symbol=';
    $input = 'AAPL';
    // TODO would http_get be better?
    $query_response = file_get_contents($quote_baseurl.$input);
    echo "Got JSON response:<br>";
    echo $query_response,"<br>";
    $json_root = json_decode($query_response);
    if ($json_root != null && $json_root->{'Status'} == 'SUCCESS') {
        
    } else {
        
    }
?>
<noscript>
</body>
