export default Ember.Controller.extend({
  session: Ember.inject.service('session'),
  actions: {
  invalidateSession() {
    this.get('session').invalidate();
  }
}
});
