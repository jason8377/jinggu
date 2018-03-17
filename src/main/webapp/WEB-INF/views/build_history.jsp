<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1,user-scalable=no, shrink-to-fit=no">
    <title>Android Build</title>
    <!-- Bootstrap/Jquery core CSS and JS -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <script src="https://code.jquery.com/jquery-3.2.1.slim.min.js" integrity="sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN" crossorigin="anonymous"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js" integrity="sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q" crossorigin="anonymous"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
  </head>
  <body>
  <main role="main" class="col-md-12 ml-sm-auto col-lg-10 pt-3 px-4">
    <h2>Build History</h2>
    <div class="dropdown show">
      <button class="btn btn-primary dropdown-toggle" type="button" id="menu1" data-toggle="dropdown">${project_name_selected}
      <span class="caret"></span></button>
      <ul class="dropdown-menu" role="menu" aria-labelledby="menu1">
	  <c:choose>
	    <c:when test="${not empty projectList}">
		  <li role="presentation"><a role="menuitem" tabindex="-1" href="history">All projects</a></li>
	      <c:forEach var="projectInfo" items="${projectList}">
	  	    <li role="presentation"><a role="menuitem" tabindex="-1" href="history?project_id=${projectInfo.project_id}">${projectInfo.project_name}</a></li>
	      </c:forEach>
	    </c:when>
	    <c:otherwise>
	      
	    </c:otherwise>
	  </c:choose>
      </ul>
    </div>
    <div class="table-responsive">       
      <table class="table table-striped table-hover table-sm">
        <thead>
          <tr>
            <th>Project</th>
            <th>Built By</th>
            <th>Build Time</th>
			<th>Duration(S)</th>
			<th>Build Log</th>
			<th>APKs</th>
			<th>Reports</th>
			<th>Parameters</th>
          </tr>
        </thead>
        <tbody>
		  <c:choose>
		    <c:when test="${not empty historyList}">
		      <c:forEach var="buildHistory" items="${historyList}">
		  	    <tr>
                  <td>${buildHistory.project_name}</td>
                  <td>${buildHistory.user}</td>
                  <td>${buildHistory.start_time}</td>
				  <td>${buildHistory.duration}</td>
				  <td>
					<a href="#" class="font-weight-bold" onclick="loadBuildLog('${pageContext.request.contextPath}/${buildHistory.build_log_path}/build_log.txt')" data-toggle="modal" data-target="#buildLogModal">View</a>
			      </td>
				  <td>
				    <c:forEach var="apkFile" items="${buildHistory.apk_list}">
					    <a href="${pageContext.request.contextPath}/${buildHistory.build_log_path}${apkFile.path}" class="font-weight-bold" style="display:block">${apkFile.diaplayName}</a>
					</c:forEach>
				  </td>
				  <td>
				    <c:forEach var="testFile" items="${buildHistory.test_result}">
					    <li><a href="${pageContext.request.contextPath}/${buildHistory.build_log_path}${testFile.path}" target="testReport" class="font-weight-bold">${testFile.diaplayName}</a></li>
					</c:forEach>
					<c:forEach var="testFile" items="${buildHistory.androidTest_result}">
					    <li><a href="${pageContext.request.contextPath}/${buildHistory.build_log_path}${testFile.path}" target="testReport" class="font-weight-bold">${testFile.diaplayName}</a>
					</c:forEach>
				  </td>
				  <td>
                    <div id="parameter_${buildHistory.build_sequence}" class="expand">
				    <c:forEach var="buildParameter" items="${buildHistory.parameterList}">
					    <li>${buildParameter.parameter_name} = ${buildParameter.parameter_value}</li>
					</c:forEach>
                    </div>
				  </td>
                </tr>
		      </c:forEach>
		    </c:when>
		    <c:otherwise>
		      <tr>
                <td colspan="8">Build history is empty.</td>
              </tr>
		    </c:otherwise>
		  </c:choose>
        </tbody>
      </table>
    </div>
  </main>
  <!-- Build Log Modal -->
  <div class="modal fade" id="buildLogModal" role="dialog">
      <div class="modal-dialog modal-lg">
          <div class="modal-content">
              <div class="modal-header">
                  <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                   <h4 class="modal-title">Build Log</h4>
              </div>
  			  <pre class="col-12 alert alert-secondary"><code class="language-html modal-body" data-lang="html" id="outputElement"></code></pre>
          </div>
      </div>
  </div>
  
  <a id="apk_download_link" href="" download style="display:none" />
  <script>
    function loadBuildLog(logUrl){
		$("#outputElement").load(logUrl);
	}
	function download_apk(buildSequence ,apkName){
		apk_download_link.href="${pageContext.request.contextPath}/build_history/" + buildSequence + "/" + apkName;
		apk_download_link.click();
	}
  </script>
  </body>
</html>
