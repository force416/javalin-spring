const routes = [
    {
        path: '/',
        component: httpVueLoader('./view/home.vue'),
    },
];

export default new VueRouter({
    routes: routes,
    base: '/'
});
