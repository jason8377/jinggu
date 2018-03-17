          <style>
	        .para_label {
              min-width: 170px !important;
              display: inline-block !important
            }
	      </style>
		  <script>
		    function validateForms(){
				if(!$("#v1_signing_enabled_yes").is(':checked') && !$("#v2_signing_enabled_yes").is(':checked')){
					alert("Please select at least one of the signature versions to use");
					return false;
				}
				return true;
			}
		  </script>
		  <div class="form-group row">
            <label for="commit_number" class="col-sm-2 col-lg-1 col-form-label para_label">Commit Number</label>
            <div class="col-sm-8 col-lg-5">
              <input type="text" class="form-control" id="commit_number" placeholder="Commit Number or Branch Name" required>
            </div>
          </div>
	      <div class="form-group row">
            <label for="keyAlias" class="col-sm-2 col-lg-1 col-form-label para_label">keyAlias</label>
            <div class="col-sm-8 col-lg-5">
              <input type="text" class="form-control" id="keyAlias" placeholder="keyAlias" required>
            </div>
          </div>
          <div class="form-group row">
            <label for="storePassword" class="col-sm-2 col-lg-1 col-form-label para_label">storePassword</label>
            <div class="col-sm-8 col-lg-5">
              <input type="password" class="form-control" id="storePassword" placeholder="storePassword" required>
            </div>
          </div>
		  <div class="form-group row">
            <label for="keyPassword" class="col-sm-2 col-lg-1 col-form-label para_label">keyPassword</label>
            <div class="col-sm-8 col-lg-5">
              <input type="password" class="form-control" id="keyPassword" placeholder="keyPassword" required>
            </div>
          </div>
		  <div class="form-group row">
            <label for="v1_signing_enabled" class="col-sm-2 col-lg-1 para_label">v1-signing-enabled</label>
            <div class="col-sm-8 col-lg-5">
              <div class="radio">
                <label class="checkbox-inline"><input type="radio" id="v1_signing_enabled_yes" name="v1_signing_enabled" value="true" checked /> Yes</label>
                <label  class="checkbox-inline" style="margin-left:20px"><input type="radio" id="v1_signing_enabled_no" name="v1_signing_enabled" value="false" /> No</label>
              </div>
            </div>
          </div>
		  <div class="form-group row">
            <label for="v2_signing_enabled" class="col-sm-2 col-lg-1 para_label">v2-signing-enabled</label>
            <div class="col-sm-8 col-lg-5">
			  <div class="radio">
                <label class="disply:inline"><input type="radio" id="v2_signing_enabled_yes" name="v2_signing_enabled" value="true" checked /> Yes</label>
                <label class="disply:inline" style="margin-left:20px"><input type="radio" id="v2_signing_enabled_no" name="v2_signing_enabled" value="false" /> No</label>
              </div>
            </div>
          </div>