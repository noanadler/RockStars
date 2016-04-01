import Ember from 'ember';
import ENV from 'safe-travels/config/environment';

export default Ember.Controller.extend({
  countries: Ember.computed('model.user.countries.[]', function() {
    return this.get('model.user.countries').map(function(c) {
      return { id: c.get('country').trim(), text: c.get('full_name').trim() }
    });
  })
});
