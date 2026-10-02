import {
    useEffect,
    useRef,
    useState,
} from 'react'
import {
    Link,
    NavLink,
    useLocation,
    useNavigate,
} from 'react-router'
import {useAuth} from '../../../auth/useAuth.ts'
import './Header.css'

function Header() {
    const navigate = useNavigate()

    const location = useLocation()

    const [termoBusca, setTermoBusca] = useState(() => {
        if (location.pathname !== '/catalogo') {
            return ''
        }

        return new URLSearchParams(location.search).get('nome') ?? ''
    })

    const buscaAlteradaPeloUsuario = useRef(false)
    const timeoutBusca = useRef<number | null>(null)

    const {
        usuario,
        carregando,
        sair,
    } = useAuth()

    async function handleLogout() {
        try {
            await sair()
            navigate('/')
        } catch (error) {
            console.error(error)
        }
    }

    function executarBusca(termo: string) {
        const nome = termo.trim()

        buscaAlteradaPeloUsuario.current = false

        if (!nome) {
            navigate('/catalogo')
            return
        }

        const parametros = new URLSearchParams({
            nome,
            pagina: '1',
        })

        navigate(`/catalogo?${parametros.toString()}`)
    }

    useEffect(() => {
        if (!buscaAlteradaPeloUsuario.current) {
            return
        }

        if (timeoutBusca.current !== null) {
            window.clearTimeout(timeoutBusca.current)
        }

        timeoutBusca.current = window.setTimeout(() => {
            executarBusca(termoBusca)
        }, 500)

        return () => {
            if (timeoutBusca.current !== null) {
                window.clearTimeout(timeoutBusca.current)
            }
        }
    }, [termoBusca])

    useEffect(() => {
        if (buscaAlteradaPeloUsuario.current) {
            return
        }

        if (location.pathname !== '/catalogo') {
            return
        }

        const nome =
            new URLSearchParams(location.search)
                .get('nome') ?? ''

        setTermoBusca(nome)
    }, [location.pathname, location.search])

    return (
        <>
            <header className="header">
                <div className="header_content">
                    <NavLink
                        className="header_logo"
                        to="/"
                        aria-label="Ir para a página inicial"
                    >
                        <img
                            src="/brand/gamevault-logo-horizontal.png"
                            alt="GameVault"
                        />
                    </NavLink>

                    <nav
                        className="header_navigation"
                        aria-label="Navegação principal"
                    >
                        <NavLink
                            className={({isActive}) =>
                                isActive
                                    ? 'header_link header_link--active'
                                    : 'header_link'
                            }
                            to="/"
                        >
                            Início
                        </NavLink>

                        <NavLink
                            className={({isActive}) =>
                                isActive
                                    ? 'header_link header_link--active'
                                    : 'header_link'
                            }
                            to="/catalogo"
                        >
                            Catálogo
                        </NavLink>

                        {!carregando && usuario && (
                            <>
                                <NavLink
                                    className={({isActive}) =>
                                        isActive
                                            ? 'header_link header_link--active'
                                            : 'header_link'
                                    }
                                    to="/favoritos"
                                >
                                    Favoritos
                                </NavLink>

                                <NavLink
                                    className={({isActive}) =>
                                        isActive
                                            ? 'header_link header_link--active'
                                            : 'header_link'
                                    }
                                    to="/lista-desejos"
                                >
                                    Lista desejos
                                </NavLink>
                            </>
                        )}
                    </nav>

                    <form
                        className="header_search"
                        role="search"
                        onSubmit={(event) => {
                            event.preventDefault()

                            if (timeoutBusca.current !== null) {
                                window.clearTimeout(timeoutBusca.current)
                            }

                            executarBusca(termoBusca)
                        }}
                    >
                        <label
                            className="header_search-label"
                            htmlFor="header-search"
                        >
                            Buscar jogos
                        </label>

                        <span
                            className="header_search-icon"
                            aria-hidden="true"
                        >
        <svg viewBox="0 0 24 24">
            <path
                d="m21 21-4.35-4.35m2.35-5.65a8 8 0 1 1-16 0 8 8 0 0 1 16 0Z"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
            />
        </svg>
    </span>

                        <input
                            id="header-search"
                            className="header_search-input"
                            type="search"
                            value={termoBusca}
                            placeholder="Busque jogos por nome..."
                            autoComplete="off"
                            onChange={(event) => {
                                buscaAlteradaPeloUsuario.current = true
                                setTermoBusca(event.target.value)
                            }}
                        />
                    </form>

                    <div className="header_actions">
                        {carregando ? null : usuario ? (
                            <>
                                <Link
                                    className="header_profile"
                                    to="/perfil"
                                    aria-label={`Abrir perfil de ${usuario.username}`}
                                    title={usuario.username}
                                >
                                    {usuario.imagemPerfil ? (
                                        <img
                                            className="header_profile-image"
                                            src={usuario.imagemPerfil}
                                            alt=""
                                        />
                                    ) : (
                                        <span
                                            className="header_profile-fallback"
                                            aria-hidden="true"
                                        >
                {usuario.username.charAt(0).toUpperCase()}
            </span>
                                    )}
                                </Link>

                                <button
                                    className="header_logout"
                                    type="button"
                                    onClick={handleLogout}
                                >
                                    Sair
                                </button>
                            </>
                        ) : (
                            <>
                                <NavLink
                                    className="header_action-link"
                                    to="/login"
                                >
                                    Entrar
                                </NavLink>

                                <NavLink
                                    className="header_signup"
                                    to="/cadastro"
                                >
                                    Criar conta
                                </NavLink>
                            </>
                        )}
                    </div>
                </div>
            </header>

            {!carregando && usuario && (
                <nav
                    className="mobile_navigation"
                    aria-label="Navegação móvel"
                >
                    <NavLink
                        className={({isActive}) =>
                            isActive
                                ? 'mobile_navigation-link mobile_navigation-link--active'
                                : 'mobile_navigation-link'
                        }
                        to="/"
                    >
                        <svg
                            className="mobile_navigation-icon"
                            viewBox="0 0 24 24"
                            aria-hidden="true"
                        >
                            <path
                                d="M3 10.5 12 3l9 7.5V21h-6v-6H9v6H3V10.5Z"
                                fill="none"
                                stroke="currentColor"
                                strokeWidth="1.8"
                                strokeLinejoin="round"
                            />
                        </svg>

                        <span>Início</span>
                    </NavLink>

                    <NavLink
                        className={({isActive}) =>
                            isActive
                                ? 'mobile_navigation-link mobile_navigation-link--active'
                                : 'mobile_navigation-link'
                        }
                        to="/catalogo"
                    >
                        <svg
                            className="mobile_navigation-icon"
                            viewBox="0 0 24 24"
                            aria-hidden="true"
                        >
                            <circle
                                cx="11"
                                cy="11"
                                r="6.5"
                                fill="none"
                                stroke="currentColor"
                                strokeWidth="1.8"
                            />

                            <path
                                d="m16 16 4 4"
                                fill="none"
                                stroke="currentColor"
                                strokeWidth="1.8"
                                strokeLinecap="round"
                            />
                        </svg>

                        <span>Explorar</span>
                    </NavLink>
                    <>
                        <NavLink
                            className={({isActive}) =>
                                isActive
                                    ? 'mobile_navigation-link mobile_navigation-link--active'
                                    : 'mobile_navigation-link'
                            }
                            to="/favoritos"
                        >
                            <svg
                                className="mobile_navigation-icon"
                                viewBox="0 0 24 24"
                                aria-hidden="true"
                            >
                                <path
                                    d="M12 20.5S4 16 4 9.5A4.5 4.5 0 0 1 12 6.7a4.5 4.5 0 0 1 8 2.8c0 6.5-8 11-8 11Z"
                                    fill="none"
                                    stroke="currentColor"
                                    strokeWidth="1.8"
                                    strokeLinejoin="round"
                                />
                            </svg>

                            <span>Favoritos</span>
                        </NavLink>

                        <NavLink
                            className={({isActive}) =>
                                isActive
                                    ? 'mobile_navigation-link mobile_navigation-link--active'
                                    : 'mobile_navigation-link'
                            }
                            to="/lista-desejos"
                        >
                            <svg
                                className="mobile_navigation-icon"
                                viewBox="0 0 24 24"
                                aria-hidden="true"
                            >
                                <path
                                    d="M5 4h14v17l-7-4-7 4V4Z"
                                    fill="none"
                                    stroke="currentColor"
                                    strokeWidth="1.8"
                                    strokeLinejoin="round"
                                />
                            </svg>

                            <span>Desejos</span>
                        </NavLink>

                        <NavLink
                            className={({isActive}) =>
                                isActive
                                    ? 'mobile_navigation-link mobile_navigation-link--active'
                                    : 'mobile_navigation-link'
                            }
                            to="/perfil"
                        >
                            <svg
                                className="mobile_navigation-icon"
                                viewBox="0 0 24 24"
                                aria-hidden="true"
                            >
                                <circle
                                    cx="12"
                                    cy="8"
                                    r="3.5"
                                    fill="none"
                                    stroke="currentColor"
                                    strokeWidth="1.8"
                                />

                                <path
                                    d="M5 20c.8-4 3.2-6 7-6s6.2 2 7 6"
                                    fill="none"
                                    stroke="currentColor"
                                    strokeWidth="1.8"
                                    strokeLinecap="round"
                                />
                            </svg>

                            <span>Perfil</span>
                        </NavLink>
                    </>
                </nav>
            )}
        </>
    )
}

export default Header