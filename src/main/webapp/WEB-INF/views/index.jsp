<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="en" ng-app="myApp" ng-controller="myCtrl">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1,user-scalable=no, shrink-to-fit=no">
	<link rel="icon" type="image/x-icon" class="js-site-favicon" href="<c:url value="//resources//favicon.ico" />">
    <title>Buildroid</title>
    <!-- Bootstrap core CSS -->
    <link href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://getbootstrap.com/assets/css/docs.min.css" rel="stylesheet">
    <style>
	  .build_button {
	    width:120px
      }
	</style>
    
    <script src="https://code.jquery.com/jquery-3.2.1.slim.min.js" integrity="sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN" crossorigin="anonymous"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js" integrity="sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl" crossorigin="anonymous"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.6.4/angular.min.js"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.6.4/angular-cookies.min.js"></script>
    <script src="<c:url value="//resources/js/buildroid.js" />"></script>
    <script>
      var app = angular.module('myApp', ['ngCookies']);
      app.controller('myCtrl', function($scope, $interval ,$http ,$cookies) {
          //Some initialization data from JSTL
          $scope.project_id_selected = '${project_id_selected}';
          $scope.websocketUrl = '${websocketUrl}';
          $scope.restUrl = '${restUrl}';
          $scope.isBuilding = ${isBuilding};
          
          var buildParameters = getBuildParameters();
          
          if(buildParameters.length>0){
              for(var i=0;i<buildParameters.length;i++){
                  var cookieName = $scope.project_id_selected+"_"+buildParameters[i][0];
                  var cookieValue = $cookies.get(cookieName);
                  build_val(buildParameters[i][0], cookieValue);
              }
          }
  
          //Get project list
          $http.get($scope.restUrl + "/getProjectList")
          .then(function(response) {
              $scope.projectList = response.data;
			  $("#errorContainer").hide();
          }, function (response) {
              $scope.errorMessage = 'Project list is empty, please check your configuration.';
          });
          
          //If some project is building
          $scope.isProjectBuilding = function() {
              return $scope.isBuilding;
          }
          
          //build function
          $scope.startBuild = function(parameters) {
			  if(typeof validateForms == 'function'){
				  if(!validateForms()){
					  return;
				  }
			  }
              var data = {
                method : "POST",
                url : $scope.restUrl+"/build",
                data : 
                    { 'project_id' : $scope.project_id_selected
                     ,'user_name' : ''
                     ,'build_parameters' : []
                    }
              };
              buildParameters = getBuildParameters();
              if(buildParameters.length>0){
                  var expireDate = new Date();
                  expireDate.setDate(expireDate.getDate() + 90);
                    for(var i=0;i<buildParameters.length;i++){
                        var cookieName = $scope.project_id_selected+"_"+buildParameters[i][0];
						var parameterValue = buildParameters[i][1];
                        $cookies.put(cookieName, parameterValue ,{'expires': expireDate});
						
						//If it is a password field, wrap it with sensitive tag
						if(buildParameters[i][3] != undefined && buildParameters[i][3] == "password"){
						    parameterValue = "<sensitive>" + parameterValue + "</sensitive>";
						}
						
                        data.data.build_parameters.push({'parameter_name' : buildParameters[i][0], 'parameter_value' : parameterValue});
                    }
              }
              $http(data).then(function (response) {
                //$scope.myWelcome = response.data;
              }, function (response) {
                //$scope.myWelcome = response.statusText;
              });
          }
          
          $scope.buildWebSocket = function() {
			  if($scope.websocketConnected)return;
              
              if ("WebSocket" in window) {
                  var ws = new WebSocket($scope.websocketUrl);
                  ws.onopen = function() {
                      $scope.websocketConnected = true;
                  };
                  ws.onmessage = function (evt) { 
                      var received_msg = evt.data;
                      $scope.parseBuildMessage(received_msg);
                  };
                          
                  ws.onclose = function() {
                      $scope.websocketConnected = false;
                  };   
                  window.onbeforeunload = function(event) {
                      socket.close();
                  };
              } else {
                  alert("WebSocket NOT supported by your Browser!");
              }
          }
		  
          $scope.parseBuildMessage = function(jsonMessage){
              try{
                  var jsonObj = JSON.parse(jsonMessage);
                  
                  //Clear the output once build is started.
                  if(jsonObj.msg_type == "build_start") {
                      //Disable build button
					  $('#build_button').prop('disabled', true);
					  $("#outputElement").html("");
                  } else if(jsonObj.msg_type == "build_end") {
                      //Enable build button
					  $('#build_button').prop('disabled', false);
                  } else if(jsonObj.msg_type == "build_output") {
                      //Disgard any messages doesn't belong to the project.
                      if(jsonObj.project_id != $scope.project_id_selected)return;
					  $("#outputElement").append(jsonObj.output + "<br>");
                  }
              }catch(Ex){}
          }
		  
          //Connect to websocket server
          $scope.buildWebSocket();
          //Check websocket connection status every 3 seconds
          $interval($scope.buildWebSocket,3000);
      });
    </script>
  </head>
  <body>
    <!--navbar navbar-expand-md navbar-dark bg-dark-->
    <nav class="navbar navbar-expand-md navbar-dark bd-navbar bg-dark">
      <a class="navbar-brand" href="#">Buildroid</a>
      <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#bd-docs-nav" aria-controls="bd-docs-nav" aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
      </button>
    </nav>

    <div class="container-fluid">
      <div class="row flex-xl-nowrap">
        <div class="col-12 col-md-4 col-xl-2 bd-sidebar" style="padding:0px">
          <nav class="bd-links" id="bd-docs-nav" style="padding-left:0px;padding-right:0px">
            <a class="nav-link {{projectInfo.project_id==project_id_selected?' alert-secondary':''}}" href="{{projectInfo.project_id}}"
               ng-repeat="projectInfo in projectList">
              {{projectInfo.project_name}}
            </a>
            <!---->
            <div id="errorContainer" class="alert">
              <strong>{{errorMessage}}</strong>
            </div>
          </nav>
        </div>
        
        <main class="col-12 col-md-8 col-xl-10 py-3 bd-content" role="main">
          <div class="alert alert-info">
            <form id="buildForm" role="form" data-toggle="validator" ng-submit="startBuild(parameter)">
              <jsp:include page="${form_view}"></jsp:include>
              <div style="margin-bottom:0px">
                <button type="submit" class="btn btn-primary build_button" id="build_button" ng-disabled="isProjectBuilding()">Build</button>
                <a href="${pageContext.request.contextPath}/history?project_id=${project_id_selected}" target="new" class="font-weight-bold" style="margin-left:30px">Build History</a>
              </div>
            </form>
          </div>
          <pre class="col-12 alert alert-secondary"><code class="language-html" data-lang="html" id="outputElement">${build_log}</code></pre>
        </main>
      </div>
    </div>
  </body>
</html>
