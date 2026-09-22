import {NavLink} from 'react-router';
import './Header.css'

function Header() {
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
                    <NavLink to="/login">Entrar</NavLink>
                </div>
            </div>
        </header>
    )
}

export default Header;