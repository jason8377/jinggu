
function build_val(parameterName, parameterValue){
    if(parameterValue == undefined)return;
    
    var parameterObj = $('#' + parameterName);
    if(parameterObj.length == 0){
        parameterObj = $("[name='"+parameterName+"']" );
    }
    if(parameterObj.length ==0)return;
    
    if(parameterObj.prop("tagName").toLowerCase() == "select" && parameterObj.attr('multiple') == "multiple"){
        $.each(parameterValue.split(","), function(i,e){
            var optionToBeUpdated = $("#"+parameterName+" option[value='" + e + "']");
            if(optionToBeUpdated.length > 0){
                $("#"+parameterName+" option[value='" + e + "']").prop("selected", true);
            }else{
                $("[name='"+parameterName+"']"+" option[value='" + e + "']").prop("selected", true);
            }
        });
    }else{
        parameterObj.val([parameterValue]);
    }
}
      
function getBuildParameters(){
    var buildParameters = $("#buildForm").find("input,output,select,textarea");
    var parameterArray = [];
    for(var i=0;i<buildParameters.length;i++){
        var tagName = $(buildParameters[i]).prop("tagName").toLowerCase();
        var type = $(buildParameters[i]).attr('type');
        
        var parameterName = $(buildParameters[i]).attr('id');
        //Radio will be "name" first
        if($(buildParameters[i]).attr('type') == "radio"){
            parameterName = $(buildParameters[i]).attr('name');
        }
        if(!validParameterName(parameterName)){
            parameterName = $(buildParameters[i]).attr('name');
        }
        if(!validParameterName(parameterName))continue;
    
        var parameterValue = $(buildParameters[i]).val();
    
        if(($(buildParameters[i]).attr('type') == "checkbox" || $(buildParameters[i]).attr('type') == "radio")
            && !$(buildParameters[i]).is(':checked')){
            parameterValue = "";
        }
    
        //If it's a radio and not checked, skip it.
        if($(buildParameters[i]).attr('type') == "radio"){
            if(existAndUpdate(parameterArray, parameterName, parameterValue)){
                continue;
            }
        }
      
        var singleParameter = [parameterName,parameterValue,tagName,type];
        parameterArray.push(singleParameter);
    }
    return parameterArray;
}

function validParameterName(parameterName){
    if(parameterName == undefined || parameterName == ""){
        return false;
    }
    return true;
}

function existAndUpdate(parameterArray, parameterName, parameterValue){
    for(var i=0;i<parameterArray.length;i++){
        if(parameterArray[i][0] == parameterName){
            if(parameterValue.length > 0){
                parameterArray[i][1] = parameterValue;
            }
            return true;
        }
    }
    return false;
}