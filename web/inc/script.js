function viewStacktrace() {
    var st = document.getElementById('stacktrace');
    if(st.style.display == '')
        st.style.display = 'none';
    else
        st.style.display = '';
}