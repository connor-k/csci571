<?php
    define('ACTION_LOOKUP', 'input');
    define('ACTION_QUOTE', 'symbol');
    //TODO parsing on this end for rounding fields and such
    if (isset($_GET[ACTION_LOOKUP])) {
        $lookup_baseurl = 'http://dev.markitondemand.com/MODApis/Api/v2/Lookup/json?input=';
        $input = urlencode(htmlspecialchars(trim($_GET[ACTION_LOOKUP])));
        // Handle bad response nicely
        $lookup_response = @file_get_contents($lookup_baseurl.$input);
        if ($lookup_response !== false) {
            printf("%s", $lookup_response);
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
            //TODO can't round change though since 0 may still need arrow... check all those
            //$received_format = 'D M j H:i:s e y';
            $display_format = 'Y-m-d g:i A';
            $timestamp = $json_root->{'Timestamp'};
            date_default_timezone_set("America/Los_Angeles");
            $timestamp = strtotime($timestamp);
            $timestamp = date($display_format, $timestamp);
            $json_root->{'Timestamp'} = $timestamp;
            // TODO more precise rounding here
            $market_cap_unit = "B";
            $market_cap = round($json_root->{'MarketCap'}/1000000000.0, 2);
            // Use millions if too small of a cap
            if ($market_cap < 1.0) {
                $market_cap = round($json_root->{'MarketCap'}/1000000.0, 2);
                $market_cap_unit = "M";
            }
            $json_root->{'MarketCap'} = number_format($market_cap, 2, '.', '')." ".$market_cap_unit;
            $json_root->{'Volume'} = number_format($json_root->{'Volume'});
            $json_root->{'High'} = number_format(round($json_root->{'High'}, 2), 2, '.', '');
            $json_root->{'Low'} = number_format(round($json_root->{'Low'}, 2), 2, '.', '');
            $json_root->{'Open'} = number_format(round($json_root->{'Open'}, 2), 2, '.', '');
            $response = json_encode($json_root, JSON_PRETTY_PRINT);
            printf("%s", $response);
        }
    }
?>