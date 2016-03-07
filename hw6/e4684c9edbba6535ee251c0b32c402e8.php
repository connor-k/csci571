<!DOCTYPE html>
<html lang="en">
<html>
<head>
    <meta charset="UTF-8">
    <title>ckerns HW6 - PHP</title>
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
            background-color: #F4F4F4;
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
        
        th {
            background-color: #F4F4F4;
        }
        
        .response_table {
            width: 560px;
            margin: 8px auto;
            border-collapse: collapse;
            border-style: solid;
            border-width: 2px;
            border-color: #C7C7C7;
            text-align: left;
            font-size: 0.9em;
            font-family: sans-serif;
        }
        
        .response_table th, .response_table td {
            padding-top: 4px;
            padding-bottom: 4px;
            border-style: solid;
            border-width: 1px;
            border-color: #CBCBCB;
        }
        
        .response_table td {
            background-color: #FBFBFB;
        }
        
        .center {
            text-align: center;
        }
        
        .col1 {
            width: 270px;
        }
        
        .col2 {
            width: 270px;
        }
        
        .symbol {
            width: 50px;
        }
        
        .exchange {
            width: 110px;
        }
        
        .more_info {
            color: blue;
            text-decoration: underline;
            width: 60px;
        }
        
        img {
            height: 13px;
            width: 13px;
        }
    </style>
    <script lang="JavaScript">
        function clearForm(f) {
            // Clear the input box
            f.search_input.value = "";
            // Also clear the response area, as noted on Piazza
            document.getElementById('query_response').innerHTML = "";
        }
        
        function submitMoreInfo(s) {
            document.getElementById('more_info').value = s;
            document.getElementById('more_info_form').submit();
        }
    </script>
</head>
<body>
    <!-- The main search box -->
    <!-- TODO check more info wrapping search 'bank' -->
    <div class="search_box">
        <span class="title">Stock Search</span>
        <hr class="line">
        <form method="POST" action="" id="stock_search">
            <table align="center" class="search_table_layout">
                <tr>
                    <td><span class="input_label">Company Name or Symbol:</span></td>
                    <!--  @328 -->
                    <td><input type="text" name="search_input" REQUIRED value="<?php echo isset($_POST['search_input']) ? $_POST['search_input'] : (isset($_POST['old_search']) ? $_POST['old_search'] : "") ?>" pattern=".*[^\s]+.*" onInvalid="setCustomValidity('Please fill out this field.')" onKeyUp="try{setCustomValidity('')}catch(e){}"></td>
                </tr>
                <tr align="left">
                    <td align="right"></td>
                    <td><input type="submit" value="Search"><input type="button" value="Clear" onclick="clearForm(this.form)"></td>
                </tr>
                <tr>
                    <td colspan=2 align="right"><a href="http://www.markit.com/product/markit-on-demand" target="_blank">Powered by Markit on Demand</a></td>
                </tr>
            </table>
        </form>
    </div>
    
    <!-- A hidden form to submit requests when the user clicks More Info after a search -->
    <!-- TODO search clear click and query back -- remove it -->
    <form method="POST" action="" id="more_info_form" hidden>
        <input type="text" name="more_info" id="more_info">
        <!-- Have a dummy field for search_input in this form to set when they click more info. This makes it so the search box doesn't change. @216 -->
        <input type="text" name="old_search" value="<?php echo isset($_POST['search_input']) ? $_POST['search_input'] : (isset($_POST['more_info']) ? $_POST['more_info'] : "") ?>">
    </form>
    
    <!-- The list of search results or detailed view for a given stock -->
    <div id="query_response">
        <?php
            if (isset($_POST['more_info'])) { //query_click
                // Quote
                $quote_baseurl = 'http://dev.markitondemand.com/MODApis/Api/v2/Quote/json?symbol=';
                $input = urlencode(htmlspecialchars(trim($_POST['more_info'])));
                $query_response = file_get_contents($quote_baseurl.$input);
                $json_root = json_decode($query_response);
                echo "<table class='response_table'>";
                if ($json_root != null && $json_root->{'Status'} == 'SUCCESS') {
                    //TODO check round before or after display
                    echo "<tr><th class='col1'>Name</th><td class='center col2'>",$json_root->{'Name'},"</tr>";
                    echo "<tr><th class='col1'>Symbol</th><td class='center col2'>",$json_root->{'Symbol'},"</tr>";
                    echo "<tr><th class='col1'>Last Price</th><td class='center col2'>",number_format(round($json_root->{'LastPrice'}, 2), 2, '.', ''),"</tr>";
                    $change = round($json_root->{'Change'}, 2);
                    echo "<tr><th class='col1'>Change</th><td class='center col2'>",number_format($change, 2, '.', ''),($change > 0 ? "<img src='Green_Arrow_Up.png' alt='Down'></img>" : ($change < 0 ? "<img src='Red_Arrow_Down.png'></img>" : "")),"</tr>";
                    $change = round($json_root->{'ChangePercent'}, 2);
                    echo "<tr><th class='col1'>Change Percent</th><td class='center col2'>",number_format($change, 2, '.', ''),"%",($change > 0 ? "<img src='Green_Arrow_Up.png' alt='Down'></img>" : ($change < 0 ? "<img src='Red_Arrow_Down.png'></img>" : "")),"</tr>";
                    //$received_format = 'D M j H:i:s e y';
                    $display_format = 'Y-m-d g:i A';
                    $timestamp = $json_root->{'Timestamp'};
                    date_default_timezone_set("America/Los_Angeles");
                    $timestamp = strtotime($timestamp);
                    $timestamp = date($display_format, $timestamp);
                    echo "<tr><th class='col1'>Timestamp</th><td class='center col2'>",$timestamp,"</tr>";
                    $market_cap_unit = "B";
                    $market_cap = round($json_root->{'MarketCap'}/1000000000.0, 2);
                    // Use millions if too small of a cap @314
                    if ($market_cap == 0.00) {
                        $market_cap = round($json_root->{'MarketCap'}/1000000.0, 2);
                        $market_cap_unit = "M";
                    }
                    echo "<tr><th class='col1'>Market Cap</th><td class='center col2'>",number_format($market_cap, 2, '.', '')," $market_cap_unit </tr>";
                    echo "<tr><th class='col1'>Volume</th><td class='center col2'>",number_format($json_root->{'Volume'}),"</tr>";
                    $ytd = round($json_root->{'LastPrice'} - $json_root->{'ChangeYTD'}, 2);
                    echo "<tr><th class='col1'>Change YTD</th><td class='center col2'>",$ytd < 0 ? "(".number_format($ytd, 2, '.', '').")" : number_format($ytd, 2, '.', ''),($ytd > 0 ? "<img src='Green_Arrow_Up.png' alt='Down'></img>" : ($ytd < 0 ? "<img src='Red_Arrow_Down.png'></img>" : "")),"</tr>";
                    $change = round($json_root->{'ChangePercentYTD'}, 2);
                    echo "<tr><th class='col1'>Change Percent YTD</th><td class='center col2'>",number_format($change, 2, '.', ''),"%",($change > 0 ? "<img src='Green_Arrow_Up.png' alt='Down'></img>" : ($change < 0 ? "<img src='Red_Arrow_Down.png'></img>" : "")),"</tr>";
                    echo "<tr><th class='col1'>High</th><td class='center col2'>",number_format(round($json_root->{'High'}, 2), 2, '.', ''),"</tr>";
                    echo "<tr><th class='col1'>Low</th><td class='center col2'>",number_format(round($json_root->{'Low'}, 2), 2, '.', ''),"</tr>";
                    echo "<tr><th class='col1'>Open</th><td class='center col2'>",number_format(round($json_root->{'Open'}, 2), 2, '.', ''),"</tr>";
                } else {
                    echo "<tr><td align='center'>There is no stock information available</td></tr>";
                }
                echo "</table>";
                // Unset so this isn't displayed if they search again.
                unset($_POST['more_info']);
            } else if (isset($_POST['search_input'])) {
                // Lookup
                $lookup_baseurl = 'http://dev.markitondemand.com/MODApis/Api/v2/Lookup/xml?input=';
                $input = urlencode(htmlspecialchars(trim($_POST['search_input'])));
                // From @286, we need to validate for empty form on Safari too. So, validate the input
                // here too in case it got past the html5 form validation (i.e. not supported) and create
                // the desired alert if the input was bad
                if (strlen($input) == 0) {
                    echo "<script>alert('Please enter Name or Symbol')</script>";
                } else {
                    $lookup_response = file_get_contents($lookup_baseurl.$input);
                    $xml_root = new SimpleXMLElement($lookup_response);

                    echo "<table class='response_table'>";
                    // Check if there are any matches
                    if (sizeof($xml_root->LookupResult) == 0) {
                        echo "<tr><td align='center'>No Record has been found</td></tr>";
                    } else {
                        echo "<tr><th class='col1'>Name</th><th class='symbol'>Symbol</th><th class='exchange'>Exchange</th><th style='width: 60px;'>Details</th></tr>";
                        for ($i = 0; $i < sizeof($xml_root->LookupResult); $i++) {
                            echo "<tr>";
                            echo "<td class='col1'>",$xml_root->LookupResult[$i]->Name,"</td>";
                            echo "<td class='symbol'>",$xml_root->LookupResult[$i]->Symbol,"</td>";
                            echo "<td class='exchange'>",$xml_root->LookupResult[$i]->Exchange,"</td>";
                            // TODO style link to not be clicked?
                            echo "<td><a class='more_info' onClick=\"submitMoreInfo('",$xml_root->LookupResult[$i]->Symbol,"')\">More Info</a></td>";
                            echo "</tr>";
                        }
                    }
                    echo "</table>";   
                }
            }
        ?>
    </div>
<noscript>
</body>
