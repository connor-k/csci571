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
        
        th {
            background-color: #F2F2F2;
        }
        
        .response_table {
            width: 640px;
            margin: 8px auto;
            border-collapse: collapse;
            border-style: solid;
            border-width: 2px;
            border-color: #C7C7C7;
            text-align: left;
        }
        
        .response_table th, .response_table td {
            padding-top: 4px;
            padding-bottom: 4px;
            border-style: solid;
            border-width: 1px;
            border-color: #CBCBCB;
        }
        
        .col1 {
            width: 350px;
        }
        
        .center {
            text-align: center;
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
    <div class="search_box">
        <span class="title">Stock Search</span>
        <hr class="line">
        <form method="POST" action="" id="stock_search">
            <table align="center" class="search_table_layout">
                <tr>
                    <td><span class="input_label">Company Name or Symbol:</span></td>
                    <!-- TODO change REQUIRED to use a dialog like example, see what they actually want... -->
                    <!-- TODO set value to more info term if click that?? -->
                    <td><input type="text" name="search_input" REQUIRED value="<?php echo isset($_POST['search_input']) ? $_POST['search_input'] : (isset($_POST['more_info']) ? $_POST['more_info'] : "") ?>"></td>
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
    <form method="POST" action="" id="more_info_form" hidden>
        <input type="text" name="more_info" id="more_info">
    </form>
    
    <!-- The list of search results or detailed view for a given stock -->
    <div id="query_response">
        <?php
            if (isset($_POST['more_info'])) { //query_click
                // Quote
                $quote_baseurl = 'http://dev.markitondemand.com/MODApis/Api/v2/Quote/json?symbol=';
                $input = $_POST['more_info'];
                $query_response = file_get_contents($quote_baseurl.$input);
                $json_root = json_decode($query_response);
                echo "<table class='response_table'>";
                if ($json_root != null && $json_root->{'Status'} == 'SUCCESS') {
                    // TODO timestamp format
                    echo "<tr><th class='col1'>Name</th><td class='center'>",$json_root->{'Name'},"</tr>";
                    echo "<tr><th class='col1'>Symbol</th><td class='center'>",$json_root->{'Symbol'},"</tr>";
                    echo "<tr><th class='col1'>Last Price</th><td class='center'>",number_format(round($json_root->{'LastPrice'}, 2), 2, '.', ''),"</tr>";
                    $change = round($json_root->{'Change'}, 2);
                    echo "<tr><th class='col1'>Change</th><td class='center'>",number_format($change, 2, '.', ''),($change > 0 ? "<img src='Green_Arrow_Up.png' alt='Down'></img>" : ($change < 0 ? "<img src='Red_Arrow_Down.png'></img>" : "")),"</tr>";
                    $change = round($json_root->{'ChangePercent'}, 2);
                    echo "<tr><th class='col1'>Change Percent</th><td class='center'>",number_format($change, 2, '.', ''),"%",($change > 0 ? "<img src='Green_Arrow_Up.png' alt='Down'></img>" : ($change < 0 ? "<img src='Red_Arrow_Down.png'></img>" : "")),"</tr>";
                    echo "<tr><th class='col1'>Timestamp</th><td class='center'>",$json_root->{'Timestamp'},"</tr>"; //TODO format
                    echo "<tr><th class='col1'>Market Cap</th><td class='center'>",number_format(round($json_root->{'MarketCap'}/1000000000.0, 2), 2, '.', '')," B</tr>";
                    echo "<tr><th class='col1'>Volume</th><td class='center'>",number_format($json_root->{'Volume'}),"</tr>";
                    $ytd = round($json_root->{'LastPrice'} - $json_root->{'ChangeYTD'}, 2);
                    echo "<tr><th class='col1'>Change YTD</th><td class='center'>",$ytd < 0 ? "(".number_format($ytd, 2, '.', '').")" : number_format($ytd, 2, '.', ''),($ytd > 0 ? "<img src='Green_Arrow_Up.png' alt='Down'></img>" : ($ytd < 0 ? "<img src='Red_Arrow_Down.png'></img>" : "")),"</tr>";
                    $change = round($json_root->{'ChangePercentYTD'}, 2);
                    echo "<tr><th class='col1'>Change Percent YTD</th><td class='center'>",number_format($change, 2, '.', ''),"%",($change > 0 ? "<img src='Green_Arrow_Up.png' alt='Down'></img>" : ($change < 0 ? "<img src='Red_Arrow_Down.png'></img>" : "")),"</tr>";
                    echo "<tr><th class='col1'>High</th><td class='center'>",number_format(round($json_root->{'High'}, 2), 2, '.', ''),"</tr>";
                    echo "<tr><th class='col1'>Low</th><td class='center'>",number_format(round($json_root->{'Low'}, 2), 2, '.', ''),"</tr>";
                    echo "<tr><th class='col1'>Open</th><td class='center'>",number_format(round($json_root->{'Open'}, 2), 2, '.', ''),"</tr>";
                } else {
                    echo "<tr><td align='center'>There is no stock information available</td></tr>";
                }
                echo "</table>";
                // Unset so this isn't displayed if they search
                unset($_POST['more_info']);
            } else if (isset($_POST['search_input'])) {
                // TODO clear query variable
                // Lookup
                $lookup_baseurl = 'http://dev.markitondemand.com/MODApis/Api/v2/Lookup/xml?input=';
                $input = $_POST['search_input'];
                $lookup_response = file_get_contents($lookup_baseurl.$input);
                $xml_root = new SimpleXMLElement($lookup_response);
                
                echo "<table class='response_table'>";
                // Check if there are any matches
                if (sizeof($xml_root->LookupResult) == 0) {
                    echo "<tr><td align='center'>No Record has been found</td></tr>";
                } else {
                    echo "<tr><th class='col1'>Name</th><th>Symbol</th><th>Exchange</th><th>Details</th></tr>";
                    for ($i = 0; $i < sizeof($xml_root->LookupResult); $i++) {
                        echo "<tr>";
                        echo "<td class='col1'>",$xml_root->LookupResult[$i]->Name,"</td>";
                        echo "<td>",$xml_root->LookupResult[$i]->Symbol,"</td>";
                        echo "<td>",$xml_root->LookupResult[$i]->Exchange,"</td>";
                        // TODO style link to not be clicked?
                        echo "<td><a onClick=\"submitMoreInfo('",$xml_root->LookupResult[$i]->Symbol,"')\" href='",$xml_root->LookupResult[$i]->Symbol,"'>More Info</a></td>";
                        echo "</tr>";
                    }
                }
                echo "</table>";
            }
        ?>
    </div>
<noscript>
</body>
