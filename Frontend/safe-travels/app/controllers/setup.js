import Ember from 'ember';
import ENV from 'safe-travels/config/environment';

export default Ember.Controller.extend({
  countries: Ember.computed('model.user.countries.[]', function() {
    console.log("IN HERE");
    return this.get('model.user.countries').map(function(c) {
      console.log(c);
      return { id: c.get('country').trim(), text: c.get('full_name').trim() }
    });
  })
});
