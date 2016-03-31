<?php
    define('ACTION_LOOKUP', 'input');
    define('ACTION_QUOTE', 'symbol');

    if (isset($_GET[ACTION_LOOKUP])) {
        $lookup_baseurl = 'http://dev.markitondemand.com/MODApis/Api/v2/Lookup/json?input=';
        $input = urlencode(htmlspecialchars(trim($_GET[ACTION_LOOKUP])));
        // Handle bad response nicely
        $lookup_response = @file_get_contents($lookup_baseurl.$input);
        // Preprocess the response to get the desired list format
        if ($lookup_response !== false) {
            $json_root = json_decode($lookup_response);
        } else {
            $json_root = null;
        }
        if ($json_root != null) {
            for ($i = 0; $i < sizeof($json_root); $i++) {
                $json_root[$i]->{'Display'} = $json_root[$i]->{'Symbol'}." - ".$json_root[$i]->{'Name'}." ( ".$json_root[$i]->{'Exchange'}." )";
            }

            $response = json_encode($json_root, JSON_PRETTY_PRINT);
            printf("%s", $response);
        }
    } else if (isset($_GET[ACTION_QUOTE])) {
        $quote_baseurl = 'http://dev.markitondemand.com/MODApis/Api/v2/Quote/json?symbol=';
        $input = urlencode(htmlspecialchars(trim($_GET[ACTION_QUOTE])));
        $query_response = @file_get_contents($quote_baseurl.$input);
        if ($query_response !== false) {
            $json_root = json_decode($query_response);
        } else {
            $json_root = null;
        }
        // Do the number processing (ex: rounding) on the server side
        if ($json_root != null && $json_root->{'Status'} == 'SUCCESS') {
            $json_root->{'LastPrice'} = number_format(round($json_root->{'LastPrice'}, 2), 2, '.', '');
            $json_root->{'Change'} = number_format(round($json_root->{'Change'}, 2), 2, '.', '');
            $json_root->{'ChangePercent'} = number_format(round($json_root->{'ChangePercent'}, 2), 2, '.', '');
            //$received_format = 'D M j H:i:s e y';
            $display_format = 'Y-m-d g:i A';
            $timestamp = $json_root->{'Timestamp'};
            date_default_timezone_set("America/Los_Angeles");
            $timestamp = strtotime($timestamp);
            $timestamp = date($display_format, $timestamp);
            $json_root->{'Timestamp'} = $timestamp;
            // TODO more precise rounding here
            $market_cap_unit = "Billion";
            $market_cap = round($json_root->{'MarketCap'}/1000000000.0, 2);
            // Use millions if too small of a cap
            if ($market_cap < 1.0) {
                $market_cap = round($json_root->{'MarketCap'}/1000000.0, 2);
                $market_cap_unit = "Million";
            }
            // If < 1 M, no unit
            if ($market_cap < 1.0) {
                $market_cap = round($json_root->{'MarketCap'}, 2);
                $market_cap_unit = "";
            }
            $json_root->{'MarketCap'} = number_format($market_cap, 2, '.', '')." ".$market_cap_unit;
            $json_root->{'Volume'} = number_format($json_root->{'Volume'});
            $json_root->{'ChangeYTD'} = number_format(round($json_root->{'LastPrice'} - $json_root->{'ChangeYTD'}, 2), 2, '.', '');
            $json_root->{'ChangePercentYTD'} = number_format(round($json_root->{'ChangePercentYTD'}, 2), 2, '.', '');
            $json_root->{'High'} = number_format(round($json_root->{'High'}, 2), 2, '.', '');
            $json_root->{'Low'} = number_format(round($json_root->{'Low'}, 2), 2, '.', '');
            $json_root->{'Open'} = number_format(round($json_root->{'Open'}, 2), 2, '.', '');
            $response = json_encode($json_root, JSON_PRETTY_PRINT);
            printf("%s", $response);
        }
    }
?>