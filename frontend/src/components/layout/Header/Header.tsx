import {
    Link,
    NavLink,
    useNavigate,
} from 'react-router'
import {useAuth} from '../../../auth/useAuth.ts'
import './Header.css'

function Header() {
    const navigate = useNavigate()

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

    return (
        <header className="header">
            <div className="header_content">
                <NavLink className="header_logo" to="/">
                    GameVault
                </NavLink>

                <nav className="header_navigation">
                    <NavLink
                        className={({isActive}) =>
                            isActive ? 'header_link header_link--active' : 'header_link'
                        }
                        to="/"
                    >
                        Início
                    </NavLink>

                    <NavLink
                        className={({isActive}) =>
                            isActive ? 'header_link header_link--active' : 'header_link'
                        }
                        to="/favoritos"
                    >
                        Favoritos
                    </NavLink>

                    <NavLink
                        className={({isActive}) =>
                            isActive ? 'header_link header_link--active' : 'header_link'
                        }
                        to="/lista-desejos"
                    >
                        Lista desejos
                    </NavLink>
                </nav>

                <div className="header_actions">
                    {carregando ? null : usuario ? (
                        <>
                            <span>{usuario.username}</span>
                            <Link to="/perfil">Perfil</Link>
                            <button
                                type="button"
                                onClick={handleLogout}
                            >
                                Sair
                            </button>
                        </>
                    ) : (
                        <NavLink to="/login">
                            Entrar
                        </NavLink>
                    )}
                </div>
            </div>
        </header>
    )
}

export default Header;