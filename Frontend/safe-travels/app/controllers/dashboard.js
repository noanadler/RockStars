import Ember from 'ember';
import ENV from 'safe-travels/config/environment';

export default Ember.Controller.extend({
  session: Ember.inject.service(),
  tripVaccines: Ember.computed('model.vaccines.[]', 'model.completedVaccines.[]', function() {
    var vaccines = this.get('model.vaccines');
    var completed = this.get('model.completedVaccines');
    vaccines = vaccines.map(function(v) {
      if(completed.mapBy('name').contains(v.get('name'))) {
        var completedVac = completed.findBy('name', v.get('name'))
        v.set('vaccinated', true);
        if(completedVac) {
          v.set('vaccinatedDate', completedVac.vaccinated_date);
        }
      }
      return v;
    });

    return vaccines;
  }),
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
      var model = this;
      this.get('session').authorize('authorizer:token', (header, token) => {
        headers[header] = token;
      });

      var date = vaccinatedDate.toISOString().slice(0,10).split('-')
      model.get('content.completedVaccines').pushObject({
        name: vaccine.get('name'),
        vaccinated_date: date[1] + "/" + date[2] + "/" + date[0]
      });

      Ember.$.ajax({
        url: ENV.APP.apiUrl + '/vaccine/add',
        method: 'POST',
        headers: headers,
        data: { vaccine: vaccine.get('name'), vaccinatedDate: vaccinatedDate.toISOString().slice(0,10), uuid: this.get('model.id') }
      }).then(function() {
        vaccine.set('vaccinated', true)
      });
    }
  }
});
