import {useEffect, useState} from 'react'
import {useParams} from 'react-router'
import {buscarDetalhesJogo} from '../../api/jogosApi.ts'
import type {JogoDetalhes} from '../../types/jogo.ts'
import {
    adicionarFavorito,
    adicionarListaDesejos,
    removerFavorito,
    removerListaDesejos,
} from '../../api/colecoesApi.ts'
import './JogoDetalhesPage.css'

function JogoDetalhesPage() {
    const {rawgGameId} = useParams()

    const [jogo, setJogo] = useState<JogoDetalhes | null>(null)
    const [carregando, setCarregando] = useState(true)
    const [erro, setErro] = useState<string | null>(null)

    const [processandoFavorito, setProcessandoFavorito] =
        useState(false)

    const [processandoListaDesejos, setProcessandoListaDesejos] =
        useState(false)

    const [erroAcao, setErroAcao] =
        useState<string | null>(null)

    useEffect(() => {
        async function carregarJogo() {
            const id = Number(rawgGameId)

            if (!rawgGameId || Number.isNaN(id) || id < 1) {
                setErro('Identificador de jogo inválido.')
                setCarregando(false)
                return
            }

            try {
                setCarregando(true)
                setErro(null)

                const resposta = await buscarDetalhesJogo(id)

                setJogo(resposta)
            } catch (error) {
                console.error(error)
                setErro('Não foi possível carregar os detalhes do jogo.')
            } finally {
                setCarregando(false)
            }
        }

        carregarJogo()
    }, [rawgGameId])

    async function alternarFavorito() {
        if (!jogo || jogo.favoritado === null) {
            return
        }

        const novoEstado = !jogo.favoritado

        try {
            setProcessandoFavorito(true)
            setErroAcao(null)

            if (jogo.favoritado) {
                await removerFavorito(jogo.rawgGameId)
            } else {
                await adicionarFavorito(jogo.rawgGameId)
            }

            setJogo((jogoAtual) =>
                jogoAtual
                    ? {
                        ...jogoAtual,
                        favoritado: novoEstado,
                    }
                    : jogoAtual,
            )
        } catch (error) {
            console.error(error)
            setErroAcao(
                'Não foi possível atualizar os favoritos.',
            )
        } finally {
            setProcessandoFavorito(false)
        }
    }

    async function alternarListaDesejos() {
        if (!jogo || jogo.naListaDesejos === null) {
            return
        }

        const novoEstado = !jogo.naListaDesejos

        try {
            setProcessandoListaDesejos(true)
            setErroAcao(null)

            if (jogo.naListaDesejos) {
                await removerListaDesejos(jogo.rawgGameId)
            } else {
                await adicionarListaDesejos(jogo.rawgGameId)
            }

            setJogo((jogoAtual) =>
                jogoAtual
                    ? {
                        ...jogoAtual,
                        naListaDesejos: novoEstado,
                    }
                    : jogoAtual,
            )
        } catch (error) {
            console.error(error)
            setErroAcao(
                'Não foi possível atualizar a lista de desejos.',
            )
        } finally {
            setProcessandoListaDesejos(false)
        }
    }

    if (carregando) {
        return (
            <main className="jogo-detalhes">
                <p>Carregando jogo...</p>
            </main>
        )
    }

    if (erro) {
        return (
            <main className="jogo-detalhes">
                <p>{erro}</p>
            </main>
        )
    }

    if (!jogo) {
        return null
    }

    const usuarioAutenticado =
        jogo.favoritado !== null &&
        jogo.naListaDesejos !== null

    return (
        <main className="jogo-detalhes">
            {jogo.imagemFundo && (
                <img
                    className="jogo-detalhes_imagem"
                    src={jogo.imagemFundo}
                    alt={`Imagem de ${jogo.nome}`}
                />
            )}

            <h1>{jogo.nome}</h1>

            {jogo.dataLancamento && (
                <p>
                    <strong>Lançamento:</strong>{' '}
                    {jogo.dataLancamento}
                </p>
            )}

            {jogo.notaRawg !== null && (
                <p>
                    <strong>Avaliação RAWG:</strong>{' '}
                    {jogo.notaRawg}
                </p>
            )}

            {jogo.metacritic !== null && (
                <p>
                    <strong>Metacritic:</strong>{' '}
                    {jogo.metacritic}
                </p>
            )}

            {jogo.tempoMedioJogo !== null && (
                <p>
                    <strong>Tempo médio:</strong>{' '}
                    {jogo.tempoMedioJogo} horas
                </p>
            )}

            {jogo.classificacaoEtaria && (
                <p>
                    <strong>Classificação etária:</strong>{' '}
                    {jogo.classificacaoEtaria}
                </p>
            )}

            <section>
                <h2>GameVault</h2>

                {jogo.quantidadeAvaliacoesGameVault > 0 ? (
                    <>
                        <p>
                            <strong>Avaliação dos usuários:</strong>{' '}
                            {jogo.mediaAvaliacoesGameVault}
                        </p>

                        <p>
                            <strong>Quantidade de avaliações:</strong>{' '}
                            {jogo.quantidadeAvaliacoesGameVault}
                        </p>
                    </>
                ) : (
                    <p>Este jogo ainda não possui avaliações no GameVault.</p>
                )}

                {usuarioAutenticado ? (
                    <>
                        <p>
                            <strong>Minha avaliação:</strong>{' '}
                            {jogo.minhaAvaliacao !== null
                                ? jogo.minhaAvaliacao
                                : 'Ainda não avaliado'}
                        </p>

                        <div className="jogo-detalhes__acoes">
                            <button
                                type="button"
                                onClick={alternarFavorito}
                                disabled={processandoFavorito}
                            >
                                {processandoFavorito
                                    ? 'Atualizando...'
                                    : jogo.favoritado
                                        ? 'Remover dos favoritos'
                                        : 'Adicionar aos favoritos'}
                            </button>

                            <button
                                type="button"
                                onClick={alternarListaDesejos}
                                disabled={processandoListaDesejos}
                            >
                                {processandoListaDesejos
                                    ? 'Atualizando...'
                                    : jogo.naListaDesejos
                                        ? 'Remover da lista de desejos'
                                        : 'Adicionar à lista de desejos'}
                            </button>
                        </div>

                        {erroAcao && (
                            <p role="alert">
                                {erroAcao}
                            </p>
                        )}
                    </>
                ) : (
                    <p>
                        Entre na sua conta para favoritar, adicionar à lista de desejos
                        e avaliar este jogo.
                    </p>
                )}
            </section>

            {jogo.generos.length > 0 && (
                <section>
                    <h2>Gêneros</h2>
                    <p>{jogo.generos.join(', ')}</p>
                </section>
            )}

            {jogo.plataformas.length > 0 && (
                <section>
                    <h2>Plataformas</h2>

                    <ul>
                        {jogo.plataformas.map((plataforma) => (
                            <li key={plataforma.id}>
                                {plataforma.nome}
                            </li>
                        ))}
                    </ul>
                </section>
            )}

            {jogo.desenvolvedoras.length > 0 && (
                <section>
                    <h2>Desenvolvedoras</h2>
                    <p>{jogo.desenvolvedoras.join(', ')}</p>
                </section>
            )}

            {jogo.publicadoras.length > 0 && (
                <section>
                    <h2>Publicadoras</h2>
                    <p>{jogo.publicadoras.join(', ')}</p>
                </section>
            )}

            {jogo.descricao && (
                <section>
                    <h2>Descrição</h2>
                    <p>{jogo.descricao}</p>
                </section>
            )}

            {jogo.screenshots.length > 0 && (
                <section>
                    <h2>Screenshots</h2>

                    <div className="jogo-detalhes__screenshots">
                        {jogo.screenshots.map((screenshot) => (
                            <img
                                key={screenshot}
                                src={screenshot}
                                alt={`Screenshot de ${jogo.nome}`}
                            />
                        ))}
                    </div>
                </section>
            )}
        </main>
    )
}

export default JogoDetalhesPage