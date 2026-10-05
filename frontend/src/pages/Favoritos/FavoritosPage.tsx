import {Link} from 'react-router'
import {useEffect, useState} from 'react'
import {listarFavoritos} from '../../api/colecoesApi.ts'
import GameSection, {
    type GameSectionItem,
} from '../../components/game/GameSection/GameSection.tsx'
import {converterItemColecaoParaGameSectionItem} from '../../mappers/jogoMapper.ts'
import '../colecao.css'

function FavoritosPage() {
    const [favoritos, setFavoritos] =
        useState<GameSectionItem[]>([])

    const [carregando, setCarregando] =
        useState(true)

    const [erro, setErro] =
        useState<string | null>(null)

    useEffect(() => {
        async function carregarFavoritos() {
            try {
                setCarregando(true)
                setErro(null)

                const resposta =
                    await listarFavoritos()

                setFavoritos(
                    resposta.map(
                        converterItemColecaoParaGameSectionItem,
                    ),
                )
            } catch (error) {
                console.error(error)

                setErro(
                    'Não foi possível carregar favoritos',
                )
            } finally {
                setCarregando(false)
            }
        }

        carregarFavoritos()
    }, [])

    if (carregando) {
        return (
            <main className="colecao-page">
                <p>Carregando favoritos...</p>
            </main>
        )
    }

    if (erro) {
        return (
            <main className="colecao-page">
                <p role="alert">{erro}</p>
            </main>
        )
    }

    return (
        <main className="colecao-page">
            <div className="colecao-cabecalho">
                <h1>Favoritos</h1>
                {favoritos.length > 0 && (
                    <span className="colecao-contagem">{favoritos.length}</span>
                )}
            </div>

            {favoritos.length > 0 ? (
                <GameSection
                    titulo="Seus jogos favoritos"
                    jogos={favoritos}
                />
            ) : (
                <div className="colecao-vazio">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"
                         strokeWidth="1.8" strokeLinejoin="round" aria-hidden="true">
                        <path d="M12 20.5S4 16 4 9.5A4.5 4.5 0 0 1 12 6.7a4.5 4.5 0 0 1 8 2.8c0 6.5-8 11-8 11Z"/>
                    </svg>
                    <p>Você ainda não adicionou jogos aos favoritos.</p>
                    <Link to="/catalogo">Explorar catálogo</Link>
                </div>
            )}
        </main>
    )
}

export default FavoritosPage