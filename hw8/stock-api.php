<?php
    define('ACTION_LOOKUP', 'input');
    define('ACTION_QUOTE', 'symbol');
    define('ACTION_NEWS', 'news');
    define('ACTION_CHART', 'chart');

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
            $display_format = 'd F Y, H:i:s a';
            date_default_timezone_set("America/Los_Angeles");
            $json_root->{'Timestamp'} = date($display_format, strtotime($json_root->{'Timestamp'}));
            $market_cap_unit = "Billion";
            $market_cap = round($json_root->{'MarketCap'}/1000000000.0, 2);
            // Use millions if below 1 Billion
            if ($market_cap < 1.0) {
                $market_cap = round($json_root->{'MarketCap'}/1000000.0, 2);
                $market_cap_unit = "Million";
            }
            // If < 1 M, no unit
            if ($market_cap < 1.0) {
                $market_cap = $json_root->{'MarketCap'};
                $market_cap_unit = "";
            } else {
                $market_cap = number_format($market_cap, 2, '.', '');
            }

            $json_root->{'MarketCap'} = $market_cap." ".$market_cap_unit;
            $json_root->{'ChangeYTD'} = number_format(round($json_root->{'LastPrice'} - $json_root->{'ChangeYTD'}, 2), 2, '.', '');
            $json_root->{'ChangePercentYTD'} = number_format(round($json_root->{'ChangePercentYTD'}, 2), 2, '.', '');
            $json_root->{'High'} = number_format(round($json_root->{'High'}, 2), 2, '.', '');
            $json_root->{'Low'} = number_format(round($json_root->{'Low'}, 2), 2, '.', '');
            $json_root->{'Open'} = number_format(round($json_root->{'Open'}, 2), 2, '.', '');
            $response = json_encode($json_root, JSON_PRETTY_PRINT);
            printf("%s", $response);
        } else if ($json_root != null && strpos($json_root->{'Status'}, 'Failure') !== false) {
            printf("%s", $query_response);
        }
    } else if (isset($_GET[ACTION_NEWS])) {
        $key = '9BuSpXwlNTHIMM5KOAo9RyhoJBmV/hFOKwRnHvcRSnU';
        $news_baseurl = 'https://api.datamarket.azure.com/Bing/Search/v1/News?Query=%27';
        $news_endurl = '%27&$format=json';
        $input = urlencode(htmlspecialchars(trim($_GET[ACTION_NEWS])));
        $cred = sprintf('Authorization: Basic %s', base64_encode($key.":".$key));
        $context = stream_context_create(array(
            'http' => array(
                'header'  => $cred
            )
        ));
        $response = @file_get_contents($news_baseurl.$input.$news_endurl, 0, $context);
        //$response = str_replace("\"", "\\\"", $response);
        $response = str_replace("â€™", "\\'", $response);
        printf("%s", $response);
    } else if (isset($_GET[ACTION_CHART])) {
        $chart_baseurl = 'http://dev.markitondemand.com/MODApis/Api/v2/InteractiveChart/json?parameters=%7b%22Normalized%22:false,%22NumberOfDays%22:1095,%22DataPeriod%22:%22Day%22,%22Elements%22:%5b%7b%22Symbol%22:%22';
        $chart_endurl = '%22,%22Type%22:%22price%22,%22Params%22:%5b%22ohlc%22%5d%7d%5d%7d';
        $input = $_GET[ACTION_CHART];
        $response = @file_get_contents($chart_baseurl.$input.$chart_endurl);
        printf("%s", $response);
    }
?>