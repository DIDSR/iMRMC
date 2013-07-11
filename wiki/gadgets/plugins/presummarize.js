/**
 * @projectDescription Pre-processing plug-in for summary generation, to be used with wikignpi's gadget wikigateway
 * @link http://code.google.com/p/wikignpi
 * @author Bruno Santos
 * @license MIT License <http://www.opensource.org/licenses/mit-license.php>
 *
 * Usage:
 *   Shown here: http://code.google.com/p/wikignpi/wiki/GadgetItPlugins#presummarize_-_Limit_the_number_of_level_1_and_2_headings
 */

/*
 * Possible global variables:
 * ___presummarize_numH12 - array(2) of integers >= 0. Default: [1,3] - will show the first <h1> and its 3 children <h2>.
 * ___presummarize_message - string that defines the text that usually says "Read more".
 * ___presummarize_splittextH1 - string that defines the text to be shown in place of the removed <h1> section.
 * ___presummarize_splittextH2 - string that defines the text to be shown in place of the removed <h2> section.
 */
var presummarize_numH12_default      = [1,3];
var presummarize_message_default     = 'Click here to read more...';
var presummarize_splittextH1_default = '<br>';
var presummarize_splittextH2_default = '<br>(...)<br>';

function presummarize_snipeOutLines(dLines, firstLine, lastLine, text2inject){
  dLines.splice(firstLine, lastLine-firstLine+1, text2inject);
};

function presummarize_takeoutExcess(wikitext){
  if(typeof ___presummarize_numH12 == "undefined") ___presummarize_numH12 = presummarize_numH12_default;

  //sanitize the array
  if(___presummarize_numH12.length != 2) {
    if(___presummarize_numH12.length == 0) ___presummarize_numH12 = presummarize_numH12_default;
    else if(___presummarize_numH12.length == 1) ___presummarize_numH12[1] = 0;
    //ignore all other sizes... now sanitize the size
    if(___presummarize_numH12[0]<0) ___presummarize_numH12[0] = 0;
    if(___presummarize_numH12[1]<0) ___presummarize_numH12[1] = 0;
  }
  var numH12 = ___presummarize_numH12; //simplify name

  //sanitize the splitting texts
  if(typeof ___presummarize_splittextH1 == "undefined") ___presummarize_splittextH1 = presummarize_splittextH1_default;
  if(typeof ___presummarize_splittextH2 == "undefined") ___presummarize_splittextH2 = presummarize_splittextH2_default;

  //search for the lines that start with =  and ==
  var rxH1 = /^\s*=\s*[^<>=]+?\s*=/;
  var rxH2 = /^\s*==\s*[^<>=]+?\s*==/;
  var wikilines = wikitext.split(/\n/); //have to split the text into an array, so we can regex each line

  var countH1 = 0, countH2=0, H2start=-1, j;
  for(j in wikilines) {
    if(wikilines[j].length>0) {
      var line = wikilines[j];

      //check for <h1>
      var ma = rxH1.exec(line);
      if(ma != null) {
        if(H2start >= 0 && countH2 > numH12[1]) {
          //snipe out excess child <h2>
          presummarize_snipeOutLines(wikilines, H2start, parseInt(j)-1, ___presummarize_splittextH2);
        }
        countH1++;
        countH2=0;
        H2start=-1;
        if(numH12[0] < countH1) {
          //remove everything forward
          presummarize_snipeOutLines(wikilines, j, wikilines.length-1, ___presummarize_splittextH1);
          break;
        }
        continue;
      }

      //check for <h2>
      var ma = rxH2.exec(line);
      if(ma != null) {
        countH2++;
        if(numH12[1] > countH2 && H2start<0) H2start = parseInt(j);
        continue;
      }

    }
  }
  //final snipe out
  if(H2start >= 0 && countH2 > numH12[1]) {
    //snipe out excess child <h2>
    presummarize_snipeOutLines(wikilines, H2start, parseInt(j)-1, ___presummarize_splittextH2);
  }

  //joint array into a single text once again
  wikitext = wikilines.join('\n');

  return wikitext;
};

function presummarize_addLink(wikitext){
  if(typeof ___presummarize_message == "undefined") ___presummarize_message = presummarize_message_default;
  wikitext += '<br><a target="_blank" href="' + gnpi_wikiBaseURL + gnpi_wikiname + '">' + ___presummarize_message + '</a>';
  return wikitext;
};

function presummarize_doit(wikitext) {
  wikitext = presummarize_takeoutExcess(wikitext);
  wikitext = presummarize_addLink(wikitext);
  return wikitext;
};

GNPI_registerPrePlugin('presummarize', presummarize_doit);
