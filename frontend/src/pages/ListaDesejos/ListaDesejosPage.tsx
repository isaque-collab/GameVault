import { useEffect, useState } from 'react'
import { listarListaDesejos } from '../../api/colecoesApi'
import GameSection, {
    type GameSectionItem,
} from '../../components/game/GameSection/GameSection'
import { converterItemColecaoParaGameSectionItem } from '../../mappers/jogoMapper'
import '../colecao.css'

function ListaDesejosPage() {
    const [jogos, setJogos] =
        useState<GameSectionItem[]>([])

    const [carregando, setCarregando] =
        useState(true)

    const [erro, setErro] =
        useState<string | null>(null)

    useEffect(() => {
        async function carregarListaDesejos() {
            try {
                setCarregando(true)
                setErro(null)

                const resposta =
                    await listarListaDesejos()

                setJogos(
                    resposta.map(
                        converterItemColecaoParaGameSectionItem,
                    ),
                )
            } catch (error) {
                console.error(error)

                setErro(
                    'Não foi possível carregar sua lista de desejos.',
                )
            } finally {
                setCarregando(false)
            }
        }

        carregarListaDesejos()
    }, [])

    if (carregando) {
        return (
            <main className="colecao-page">
                <p>Carregando lista de desejos...</p>
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
            <h1>Lista de desejos</h1>

            {jogos.length > 0 ? (
                <GameSection
                    titulo="Sua lista de desejos"
                    jogos={jogos}
                />
            ) : (
                <p>
                    Sua lista de desejos está vazia.
                </p>
            )}
        </main>
    )
}

export default ListaDesejosPage