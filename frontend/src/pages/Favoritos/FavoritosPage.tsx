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
            <h1>Favoritos</h1>

            {favoritos.length > 0 ? (
                <GameSection
                    titulo="Seus jogos favoritos"
                    jogos={favoritos}
                />
            ) : (
                <p>
                    Você ainda não adicionou jogos aos favoritos.
                </p>
            )}
        </main>
    )
}

export default FavoritosPage