import ENV from 'safe-travels/config/environment';

export default Ember.Route.extend({
  session: Ember.inject.service(),
  model(params) {
    var router = this;
    return Ember.$.get(ENV.APP.apiUrl + '/register/' + params.uuid).then(function(params) {
      return params.success;
      //if(params.success == true) {
          /*session.authenticate(authenticator, { identification: credentials.email, password: credentials.password }).then(function() {
            //authenticated
            session.authorize('authorizer:token', (header, token) => {
              var headers = {}
              headers[header] = token;

              Ember.$.ajax({
                url: ENV.APP.apiUrl + '/currentuuid',
                headers: headers
              }).then(function(data) {
                session.set('data.user', data );
                controller.transitionToRoute('setup');
              });
            });
          });*/
      //}
    });
  }
})
