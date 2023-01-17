package app.birdsocial.birdapi.helper

/*
class JWTAuthenticationFilter(authenticationManager: AuthenticationManager) :
    UsernamePasswordAuthenticationFilter() {
    private val authenticationManager: AuthenticationManager

    init {
        this.authenticationManager = authenticationManager
        setFilterProcessesUrl("/api/services/controller/user/login")
    }

    @Throws(AuthenticationException::class)
    fun attemptAuthentication(
        req: HttpServletRequest,
        res: HttpServletResponse?
    ): Authentication {
        return try {
            val creds: User = ObjectMapper()
                .readValue(req.getInputStream(), User::class.java)
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                    creds.getUsername(),
                    creds.getPassword(),
                    ArrayList()
                )
            )
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @Throws(IOException::class)
    protected fun successfulAuthentication(
        req: HttpServletRequest?,
        res: HttpServletResponse,
        chain: FilterChain?,
        auth: Authentication
    ) {
        val token: String = JWT.create()
            .withSubject((auth.getPrincipal() as User).getUsername())
            .withExpiresAt(Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .sign(Algorithm.HMAC512(SECRET.getBytes()))
        val body: String = (auth.getPrincipal() as User).getUsername() + " " + token
        res.getWriter().write(body)
        res.getWriter().flush()
    }
}
 */