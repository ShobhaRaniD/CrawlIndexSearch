<?php
include 'SpellCorrector.php';

header('Content-Type: text/html; charset=utf-8');

$limit = 10;
$query = isset($_REQUEST['q']) ? $_REQUEST['q'] : false;
$results = false;
$additionalParameters  =  array('sort' => 'pageRankFile desc');	
 
if ($query)
{

		  require_once('solr-php-client-master/Apache/Solr/Service.php');

		  $solr = new Apache_Solr_Service('localhost', 8983, '/solr/myIndex/');

			//ping solr...

			if (!$solr->ping()) {
				    exit('Solr service not responding.');
			}

		  if (get_magic_quotes_gpc() == 1)
		  {
		    $query = stripslashes($query);
		  }

		$query = trim($query);

		  try
		  {

			if(isset($_REQUEST['pageRank']))
			    $results  =  $solr->search($query, 0,  $limit,  $additionalParameters);	
			else
			    $results = $solr->search($query, 0, $limit);

		  }
		  catch (Exception $e)
		  {
			    die("<html><head><title>SEARCH EXCEPTION</title><body><pre>{$e->__toString()}</pre></body></html>");
		  }
}

?>
<html>
  <head>
    <title>PHP Solr Client Example</title>
    <style>
	#spellcheck:hover{
		cursor: pointer;
		color:blue;		
		}
    </style>
  </head>

 <link rel="stylesheet" href="//code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">
 <script src="http://code.jquery.com/jquery-1.10.2.js"></script>
 <script src="http://code.jquery.com/ui/1.11.4/jquery-ui.js"></script>

  <body>
	<div class="ui-widget" style="margin-top:20px;margin-left:20px">
	    <form  accept-charset="utf-8" method="get" id="myForm">
	      <label for="q">Search:
	      <input id="q" name="q" list="suggester" type="text" autocomplete="off" value="<?php echo htmlspecialchars($query, ENT_QUOTES, 'utf-8'); ?>"/>	
		</label>	
		 <br>
	      Use PageRank alogrithm <input type="checkbox" name="pageRank" value="pageRank" <?php if(isset($_REQUEST['pageRank'])) echo "checked='checked'"; ?> /> <br>
	      <input type="submit"/>
	      <input type="reset" value="Reset" onclick="window.location = 'callSolr.php'"/>
	    </form>
	</div>
<?php

if($query)
{
		//calling SpellChecker.php
		$keywords = preg_split("/[\s,]+/",$query);
		$correct_query = "";

		foreach ($keywords as $word)
			$correct_query .= SpellCorrector::correct($word).' ';

		$correct_query = trim($correct_query);

		if(strcmp($query,$correct_query)!=0)
		{?>
			<br>
			<div class="ui-widget">
			<p style='font-weight:bold'> Did you mean? <span id='spellcheck' style='font-weight:normal;font-style:italic;text-decoration:underline;' onclick='didYouMean()'> <?php echo $correct_query; ?> </span></p>
			<p style='font-weight:bold'> Showing results for: <span  style='font-weight:normal'><?php echo $query; ?> </span></p>	
			</div>
			<br>
		<?php }
}


// display results
if ($results)
{
	  $total = (int) $results->response->numFound;
	  $start = min(1, $total);
	  $end = min($limit, $total);
	?>
	    <div class="ui-widget">Results <?php echo $start; ?> - <?php echo $end;?> of <?php echo $total; ?>:<br>
	    <ol>
	<?php
	  // iterate result documents
	  foreach ($results->response->docs as $doc)
	  {
		//finding the corresponding HTTP url for the doc ID
		$file_loc = htmlspecialchars($doc->id, ENT_NOQUOTES, 'utf-8');
		$file_name = substr($file_loc, strrpos($file_loc,'/')+1);
		$file_name = substr($file_name, 0, strrpos($file_name,'.'));

		if (($handle = fopen("visit.csv", "r")) !== FALSE) {
		    while (($data = fgets($handle)) !== FALSE) {
			if (strpos($data, $file_name) !== false)
				break;
		    }
		    fclose($handle);
		}
	?>
	      <li>
		<a href="<?php echo $data; ?>">Document:</a> Title - <?php echo isset($doc->title)?$doc->title:'N/A'; ?> 
		<br> Author - <?php echo isset($doc->author)?$doc->author:'N/A'; ?>
		<br> Date Created - <?php echo isset($doc->created)?$doc->created:'N/A'; ?>
		<br> Size in KB- <?php echo isset($doc->stream_size)?(($doc->stream_size)/1000):'N/A'; ?>
	      </li>
	<br>
	<?php
	  }
	?>
	    </ol>
	<?php
}
?>
  </div></body>

<script>
	var keyChar = '';
	var terms = '';
	var stopWords =["a","am","an","and","are","as","at","be","but","by","can","cannot","could","did","do","does","for","from","had","has","have","he","her","here","him","his","how","i","if","in","into","is","isnt","it","its","lets","me","my","no","not","of","off","on","once","only","or","other","ought","our","ours","out","over","own","same","she","should","shobha","so","than","that","the","their","them","then","there","these","they","this","those","o","too","u","was","we","were","what","when","where","which","who","why","with"];

	$('#q').on ('keydown', function (e) {
	
	if((e.which>=65 && e.which<=90) || e.which==8 || e.which==32)	//the character is either an alphabet/backspace/whitespace
	{
		if(e.which==8 && keyChar.length!=0)		//remove a character when backspace is pressed
			keyChar = keyChar.slice(0,-1);
		else if(e.which==32)
		{
			keyChar='';				//start suggest for the new term in the query phrase
			terms = $('#q').val();		
		}	
		else
			keyChar += String.fromCharCode(e.which);
	
		if(keyChar.length!=0 && keyChar.indexOf("\b")==-1)
		{
			var url = 'http://localhost:8983/solr/myIndex/suggest';
			$("#q").autocomplete({
				delay: 500,
				source: function(request, response) {
					$.ajax({
					  url: url,
					  data: {'wt':'json', 'q':keyChar.toLowerCase()},
					  dataType: 'jsonp',
					  jsonp: 'json.wrf',
					  success: function(msg) {
						console.log(msg);
						console.log(keyChar.toLowerCase());
						var myArr = msg['suggest']['suggest'][keyChar.toLowerCase()]['suggestions'];
						var suggestions = new Array();

						for(var i =0; i<myArr.length && i<5; i++)
						{
							if(stopWords.indexOf(myArr[i].term) == -1)
								suggestions.push(terms+' '+myArr[i].term);
						}
						console.log(suggestions);
						response(suggestions);
						}
					});	//close of ajax

				},
				select: function(event, ui) {
					//event.preventDefault();
				}
			});	//close of autocomplete
		}
	}
					
	});	//close of keydown callback

	//JS function to handle "did you mean" spell check selected
	function didYouMean()
	{
		$('#q').val($('#spellcheck').text());		
		console.log($('#q').val());	console.log($('#spellcheck').text());
		$('#myForm').submit();
	}
 </script>
</html>

