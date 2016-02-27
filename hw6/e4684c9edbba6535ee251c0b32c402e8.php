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
    </style>
    <script lang="JavaScript">
        function clearForm(f) {
            // Clear the input box
            f.search_input.value = "";
            // Also clear the response area, as noted on Piazza
            document.getElementById('query_response').innerHTML = "";
        }
    </script>
</head>
<body>
    <div class="search_box">
        <span class="title">Stock Search</span>
        <hr class="line">
        <form method="POST" action="" id="stock_search">
            <table align="center" class="search_table_layout">
                <tr>
                    <td><span class="input_label">Company Name or Symbol:</span></td>
                    <!-- TODO change REQUIRED to use a dialog like example, see what they actually want... -->
                    <td><input type="text" name="search_input" REQUIRED value="<?php echo isset($_POST['search_input']) ? $_POST['search_input'] : "" ?>"></td>
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
    <div id="query_response">
        <?php
            if (isset($_POST['search_input'])) {
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
                        echo "<td><a href='#' onclick=","#TODO","'>","More Info</a></td>";
                        echo "</tr>";
                    }   
                }
                echo "</table>";
            }

                /*/ Quote
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
            }*/
        ?>
    </div>
<noscript>
</body>
