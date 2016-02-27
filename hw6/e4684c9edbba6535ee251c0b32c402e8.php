<!DOCTYPE html>
<html lang="en">
<html>
<head>
    <meta charset="UTF-8">
    <title>ckerns HW4 - XML</title>
    <style>
        .title {
            font-size: 1.6em;
            font-style: italic;
        }
        
        .line {
            color: #C7C7C7;
            margin: 0px;
            margin-left: 2.5%;
            width: 95%;
        }
        
        .search_box {
            padding-top: 8px;
            margin: 0 auto;
            width: 405px;
            background-color: #F2F2F2;
            border-style: solid;
            border-width: 1px;
            border-color: #C7C7C7;
            text-align: center;
        }
        
        .search_table_layout {
            margin-top: 12px;
            margin-right: 65px;
            margin-bottom: 24px;
        }
    </style>
</head>
<body>
    <div class="search_box">
        <span class="title">Stock Search</span>
        <hr class="line">
        <form method="POST" action="" id="stock_search">
            <table align="center" class="search_table_layout">
                <tr>
                    <td><span class="input_label">Company Name or Symbol:</span></td>
                    <td><input type="text" ></td>
                </tr>
                <tr align="left">
                    <td align="right"></td>
                    <td><input type="submit" value="Search"><input type="reset" value="Clear"></td>
                </tr>
                <tr>
                    <td colspan=2 align="right"><a href="http://www.markit.com/product/markit-on-demand" target="_blank">Powered by Markit on Demand</a></td>
                </tr>
            </table>
        </form>
    </div>
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
