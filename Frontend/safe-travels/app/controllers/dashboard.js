import Ember from 'ember';
import ENV from 'safe-travels/config/environment';

export default Ember.Controller.extend({
  session: Ember.inject.service(),
  actions: {
    genderChanged: function(gender) {
      this.get('model').set('gender', gender);
    },
    saveProfile: function() {
      var headers = {}
      this.get('session').authorize('authorizer:token', (header, token) => {
        headers[header] = token;
      });

      Ember.$.ajax({
        url: ENV.APP.apiUrl + '/users/' + this.get('session.data.user'),
        method: 'PUT',
        headers: headers,
        contentType: "application/json; charset=utf-8",
        dataType:'json',
        data: this.get('model').toJSON()
      })
    },
    saveVaccine: function(vaccine, vaccinatedDate) {
      var headers = {}
      this.get('session').authorize('authorizer:token', (header, token) => {
        headers[header] = token;
      });

      Ember.$.ajax({
        url: ENV.APP.apiUrl + '/vaccine/add',
        method: 'POST',
        headers: headers,
        data: { vaccine: vaccine.get('name'), vaccinatedDate: vaccinatedDate, uuid: this.get('model.id') }
      }).then(function() {
        vaccine.set('vaccinated', true)
      });
    }
  }
});
