import type {ReactNode} from 'react'
import {
    Navigate,
    useLocation,
} from 'react-router'
import {useAuth} from './useAuth.ts'

type RotaProtegidaProps = {
    children: ReactNode
}

function RotaProtegida({
                           children,
                       }: RotaProtegidaProps) {
    const {usuario, carregando} = useAuth()
    const location = useLocation()

    if (carregando) {
        return (
            <main>
                <p>Carregando...</p>
            </main>
        )
    }

    if (!usuario) {
        return (
            <Navigate
                to="/login"
                replace
                state={{
                    from:
                        location.pathname +
                        location.search,
                }}
            />
        )
    }

    return children
}

export default RotaProtegida