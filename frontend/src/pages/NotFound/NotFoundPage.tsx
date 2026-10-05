import { useState, type SubmitEvent } from 'react'
import { Link, useNavigate } from 'react-router'
import './NotFoundPage.css'

function NotFoundPage() {
    const navigate = useNavigate()
    const [busca, setBusca] = useState('')

    function handleBuscar(event: SubmitEvent<HTMLFormElement>) {
        event.preventDefault()
        const nome = busca.trim()
        navigate(nome ? `/catalogo?nome=${encodeURIComponent(nome)}` : '/catalogo')
    }

    return (
        <main className="not-found">
            <span className="not-found__status">
                <i aria-hidden="true" />
                Erro 404
            </span>

            <div className="not-found__code" aria-hidden="true">404</div>

            <h1 className="not-found__title">Página não encontrada</h1>
            <p className="not-found__text">
                O endereço que você acessou não existe ou foi movido. Volte ao início ou busque o jogo que procura.
            </p>

            <form className="not-found__search" role="search" onSubmit={handleBuscar}>
                <svg viewBox="0 0 24 24" fill="none" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
                    <circle cx="11" cy="11" r="7" />
                    <path d="m20 20-3.5-3.5" />
                </svg>
                <input
                    type="search"
                    value={busca}
                    onChange={(e) => setBusca(e.target.value)}
                    placeholder="Buscar jogos..."
                    aria-label="Buscar jogos"
                />
            </form>

            <div className="not-found__actions">
                <Link className="not-found__btn not-found__btn--primary" to="/">Voltar ao início</Link>
                <Link className="not-found__btn not-found__btn--secondary" to="/catalogo">Explorar catálogo</Link>
            </div>
        </main>
    )
}

export default NotFoundPage