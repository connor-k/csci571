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
            printf("%s", $query_response);
        }
    }
?>